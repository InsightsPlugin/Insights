package dev.frankheijden.insights;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.annotations.AllowDisabling;
import dev.frankheijden.insights.api.concurrent.ChunkContainerExecutor;
import dev.frankheijden.insights.api.concurrent.ContainerExecutor;
import dev.frankheijden.insights.api.concurrent.PlayerList;
import dev.frankheijden.insights.api.concurrent.storage.WorldDistributionStorage;
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
import dev.frankheijden.insights.concurrent.ContainerExecutorService;
import dev.frankheijden.insights.listeners.BlockListener;
import dev.frankheijden.insights.listeners.ChunkListener;
import dev.frankheijden.insights.listeners.PlayerListener;
import dev.frankheijden.insights.listeners.WorldListener;
import dev.frankheijden.insights.tasks.PlayerTrackerTask;
import dev.frankheijden.minecraftreflection.MinecraftReflection;
import org.bukkit.Bukkit;
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
    private ContainerExecutor executor;
    private ChunkContainerExecutor chunkContainerExecutor;
    private PlayerList playerList;
    private WorldDistributionStorage worldDistributionStorage;
    private WorldChunkScanTracker worldChunkScanTracker;

    @Override
    public void onLoad() {
        super.onLoad();
        instance = this;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        reloadConfigs();

        playerList = new PlayerList(Bukkit.getOnlinePlayers());
        worldDistributionStorage = new WorldDistributionStorage();
        worldChunkScanTracker = new WorldChunkScanTracker();
        executor = ContainerExecutorService.newExecutor(settings.CONCURRENT_SCAN_THREADS);
        chunkContainerExecutor = new ChunkContainerExecutor(executor, worldDistributionStorage, worldChunkScanTracker);

        InsightsListener[] disableListeners = new InsightsListener[] {
                new BlockListener(this),
                new WorldListener(this)
        };

        registerEvents(disableListeners);
        registerEvents(
                new ChunkListener(this),
                new PlayerListener(this)
        );

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
                try {
                    limits.addLimit(Limit.parse(child.toFile()));
                } catch (YamlParseException ex) {
                    getLogger().severe(ex.getMessage());
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
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
    public WorldDistributionStorage getWorldDistributionStorage() {
        return worldDistributionStorage;
    }

    @Override
    public WorldChunkScanTracker getWorldChunkScanTracker() {
        return worldChunkScanTracker;
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
