package dev.frankheijden.insights;

import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.arguments.parser.ParserRegistry;
import cloud.commandframework.brigadier.CloudBrigadierManager;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.meta.SimpleCommandMeta;
import cloud.commandframework.paper.PaperCommandManager;
import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.addons.AddonManager;
import dev.frankheijden.insights.api.annotations.AllowDisabling;
import dev.frankheijden.insights.api.concurrent.ChunkContainerExecutor;
import dev.frankheijden.insights.api.concurrent.ContainerExecutor;
import dev.frankheijden.insights.api.concurrent.PlayerList;
import dev.frankheijden.insights.api.concurrent.storage.AddonStorage;
import dev.frankheijden.insights.api.concurrent.storage.WorldStorage;
import dev.frankheijden.insights.api.concurrent.tracker.AddonScanTracker;
import dev.frankheijden.insights.api.concurrent.tracker.WorldChunkScanTracker;
import dev.frankheijden.insights.api.config.Limits;
import dev.frankheijden.insights.api.config.Messages;
import dev.frankheijden.insights.api.config.Notifications;
import dev.frankheijden.insights.api.config.Settings;
import dev.frankheijden.insights.api.config.limits.Limit;
import dev.frankheijden.insights.api.config.parser.YamlParseException;
import dev.frankheijden.insights.api.listeners.InsightsListener;
import dev.frankheijden.insights.api.utils.IOUtils;
import dev.frankheijden.insights.api.utils.ReflectionUtils;
import dev.frankheijden.insights.commands.CommandInsights;
import dev.frankheijden.insights.commands.CommandScan;
import dev.frankheijden.insights.commands.CommandScanWorld;
import dev.frankheijden.insights.commands.brigadier.BrigadierHandler;
import dev.frankheijden.insights.commands.parser.MaterialArrayArgument;
import dev.frankheijden.insights.concurrent.ContainerExecutorService;
import dev.frankheijden.insights.listeners.BlockListener;
import dev.frankheijden.insights.listeners.ChunkListener;
import dev.frankheijden.insights.listeners.EntityListener;
import dev.frankheijden.insights.listeners.PaperEntityListener;
import dev.frankheijden.insights.listeners.PistonListener;
import dev.frankheijden.insights.listeners.PlayerListener;
import dev.frankheijden.insights.listeners.WorldListener;
import dev.frankheijden.insights.tasks.EntityTrackerTask;
import dev.frankheijden.insights.tasks.PlayerTrackerTask;
import dev.frankheijden.minecraftreflection.MinecraftReflection;
import io.leangen.geantyref.TypeToken;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class Insights extends InsightsPlugin {

    private static final Map<String, Class<?>> ALLOWED_DISABLED_EVENTS;

    static {
        Map<String, Class<?>> map = new HashMap<>();

        List<Method> listenerMethods = new ArrayList<>();
        listenerMethods.addAll(ReflectionUtils.getAnnotatedMethods(BlockListener.class, AllowDisabling.class));
        listenerMethods.addAll(ReflectionUtils.getAnnotatedMethods(WorldListener.class, AllowDisabling.class));

        for (Method method : listenerMethods) {
            Class<?>[] params = method.getParameterTypes();
            if (params.length != 1) continue;

            Class<?> clazz = params[0];
            map.put(clazz.getSimpleName().toUpperCase(Locale.ENGLISH), clazz);
        }

        ALLOWED_DISABLED_EVENTS = Collections.unmodifiableMap(map);
    }

    private static final String SETTINGS_FILE_NAME = "config.yml";
    private static final String MESSAGES_FILE_NAME = "messages.yml";
    private static final String EXAMPLE_LIMITS_FOLDER_NAME = "example-limits";
    private static final String LIMITS_FOLDER_NAME = "limits";

    private Settings settings;
    private Messages messages;
    private Notifications notifications;
    private Limits limits;
    private AddonManager addonManager;
    private ContainerExecutor executor;
    private ChunkContainerExecutor chunkContainerExecutor;
    private PlayerList playerList;
    private WorldStorage worldStorage;
    private AddonStorage addonStorage;
    private WorldChunkScanTracker worldChunkScanTracker;
    private AddonScanTracker addonScanTracker;
    private EntityTrackerTask entityTrackerTask;

    @Override
    public void onLoad() {
        super.onLoad();
        instance = this;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        reloadConfigs();

        addonManager = new AddonManager(this, getDataFolder().toPath().resolve("addons"));
        try {
            addonManager.createAddonsFolder();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        getServer().getScheduler().runTaskLater(this, () -> {
            try {
                addonManager.loadAddons();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }, 1);

        playerList = new PlayerList(Bukkit.getOnlinePlayers());
        worldStorage = new WorldStorage();
        addonStorage = new AddonStorage();
        worldChunkScanTracker = new WorldChunkScanTracker();
        addonScanTracker = new AddonScanTracker();
        executor = ContainerExecutorService.newExecutor(settings.SCANS_CONCURRENT_THREADS);
        chunkContainerExecutor = new ChunkContainerExecutor(executor, worldStorage, worldChunkScanTracker);

        loadCommands();

        InsightsListener[] disableListeners = new InsightsListener[] {
                new BlockListener(this),
                new WorldListener(this)
        };

        registerEvents(disableListeners);
        registerEvents(
                new ChunkListener(this),
                new PlayerListener(this)
        );

        if (PaperLib.isPaper()) {
            registerEvents(new PaperEntityListener(this));
        } else {
            registerEvents(new EntityListener(this));
            entityTrackerTask = new EntityTrackerTask(this);
            int interval = settings.SPIGOT_ENTITY_TRACKER_INTERVAL_TICKS;
            Bukkit.getScheduler().runTaskTimer(this, entityTrackerTask, 0, interval);
        }

        if (settings.APPLY_PISTON_LIMITS) {
            registerEvents(new PistonListener(this));
        }

        for (Class<?> clazz : settings.DISABLED_EVENTS) {
            HandlerList list = MinecraftReflection.of(clazz).invoke(null, "getHandlerList");
            for (InsightsListener listener : disableListeners) {
                list.unregister(listener);
            }
            getLogger().info("Unregistered listener of '" + clazz.getSimpleName() + "'");
        }

        if (settings.CHUNK_SCAN_MODE == Settings.ChunkScanMode.ALWAYS) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(this, new PlayerTrackerTask(this), 0, 1);
        }
    }

    public Optional<EntityTrackerTask> getEntityTracker() {
        return Optional.ofNullable(entityTrackerTask);
    }

    @Override
    public void reloadSettings() {
        File file = new File(getDataFolder(), SETTINGS_FILE_NAME);
        try {
            settings = Settings.load(this, file, getResource(SETTINGS_FILE_NAME)).exceptionally(getLogger());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void reloadMessages() {
        File file = new File(getDataFolder(), MESSAGES_FILE_NAME);
        try {
            messages = Messages.load(file, getResource(MESSAGES_FILE_NAME));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void reloadNotifications() {
        if (settings == null || messages == null) {
            throw new IllegalArgumentException("Settings or Messages not initialised!");
        }
        notifications = new Notifications(this);
    }

    @Override
    public void reloadLimits() {
        limits = new Limits();

        Path limitsPath = getDataFolder().toPath().resolve(LIMITS_FOLDER_NAME);
        if (!Files.exists(limitsPath)) {
            try {
                Files.createDirectory(limitsPath);
                IOUtils.copyResourceFolder(EXAMPLE_LIMITS_FOLDER_NAME, limitsPath, getClassLoader());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(limitsPath, p -> !Files.isDirectory(p))) {
            for (Path child : stream) {
                String fileName = child.getFileName().toString();
                if (!fileName.toLowerCase(Locale.ENGLISH).endsWith(".yml")) continue;
                try {
                    Limit limit = Limit.parse(child.toFile());
                    getLogger().info("Loaded limit '" + limit.getName() + "'");
                    limits.addLimit(limit);
                } catch (YamlParseException ex) {
                    getLogger().severe("Limit '" + fileName + "' could not be loaded:");
                    getLogger().severe(ex.getMessage());
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void loadCommands() {
        PaperCommandManager<CommandSender> commandManager;
        try {
            commandManager = new PaperCommandManager<>(
                    this,
                    AsynchronousCommandExecutionCoordinator.<CommandSender>newBuilder().build(),
                    Function.identity(),
                    Function.identity()
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        // Register parsers
        ParserRegistry<CommandSender> parserRegistry = commandManager.getParserRegistry();
        parserRegistry.registerParserSupplier(
                TypeToken.get(Material[].class),
                options -> new MaterialArrayArgument.MaterialArrayParser()
        );

        // Register capabilities if allowed
        if (commandManager.queryCapability(CloudBukkitCapabilities.BRIGADIER)) {
            commandManager.registerBrigadier();
            CloudBrigadierManager<CommandSender, ?> brigadierManager = commandManager.brigadierManager();
            BrigadierHandler handler = new BrigadierHandler(brigadierManager);
            handler.registerTypes();
        }
        if (commandManager.queryCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            commandManager.registerAsynchronousCompletions();
        }

        // Create Annotation Parser
        AnnotationParser<CommandSender> annotationParser = new AnnotationParser<>(
                commandManager,
                CommandSender.class,
                parameters -> SimpleCommandMeta.empty()
        );

        // Parse commands
        annotationParser.parse(new CommandInsights(this));
        annotationParser.parse(new CommandScan(this));
        annotationParser.parse(new CommandScanWorld(this));
    }

    @Override
    public Settings getSettings() {
        return settings;
    }

    @Override
    public Messages getMessages() {
        return messages;
    }

    @Override
    public Limits getLimits() {
        return limits;
    }

    @Override
    public AddonManager getAddonManager() {
        return addonManager;
    }

    @Override
    public Notifications getNotifications() {
        return notifications;
    }

    @Override
    public ContainerExecutor getExecutor() {
        return executor;
    }

    @Override
    public ChunkContainerExecutor getChunkContainerExecutor() {
        return chunkContainerExecutor;
    }

    @Override
    public PlayerList getPlayerList() {
        return playerList;
    }

    @Override
    public WorldStorage getWorldStorage() {
        return worldStorage;
    }

    @Override
    public AddonStorage getAddonStorage() {
        return addonStorage;
    }

    @Override
    public WorldChunkScanTracker getWorldChunkScanTracker() {
        return worldChunkScanTracker;
    }

    @Override
    public AddonScanTracker getAddonScanTracker() {
        return addonScanTracker;
    }

    @Override
    public Map<String, Class<?>> getAllowedDisableEvents() {
        return ALLOWED_DISABLED_EVENTS;
    }

    private void registerEvents(InsightsListener... listeners) {
        for (InsightsListener listener : listeners) {
            Bukkit.getPluginManager().registerEvents(listener, this);
        }
    }
}
