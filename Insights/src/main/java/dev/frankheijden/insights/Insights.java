package dev.frankheijden.insights;

import static dev.frankheijden.minecraftreflection.MinecraftReflectionVersion.isMin;

import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.arguments.parser.ParserRegistry;
import cloud.commandframework.brigadier.CloudBrigadierManager;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.meta.SimpleCommandMeta;
import cloud.commandframework.paper.PaperCommandManager;
import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.addons.AddonManager;
import dev.frankheijden.insights.api.concurrent.ChunkContainerExecutor;
import dev.frankheijden.insights.api.concurrent.ChunkTeleport;
import dev.frankheijden.insights.api.concurrent.PlayerList;
import dev.frankheijden.insights.api.concurrent.count.RedstoneUpdateCount;
import dev.frankheijden.insights.api.concurrent.storage.AddonStorage;
import dev.frankheijden.insights.api.concurrent.storage.ScanHistory;
import dev.frankheijden.insights.api.concurrent.storage.WorldStorage;
import dev.frankheijden.insights.api.concurrent.tracker.AddonScanTracker;
import dev.frankheijden.insights.api.concurrent.tracker.WorldChunkScanTracker;
import dev.frankheijden.insights.api.config.Limits;
import dev.frankheijden.insights.api.config.Messages;
import dev.frankheijden.insights.api.config.Notifications;
import dev.frankheijden.insights.api.config.Settings;
import dev.frankheijden.insights.api.config.limits.Limit;
import dev.frankheijden.insights.api.config.parser.YamlParseException;
import dev.frankheijden.insights.api.metrics.MetricsManager;
import dev.frankheijden.insights.api.objects.wrappers.ScanObject;
import dev.frankheijden.insights.api.tasks.UpdateCheckerTask;
import dev.frankheijden.insights.api.utils.IOUtils;
import dev.frankheijden.insights.commands.CommandCancelScan;
import dev.frankheijden.insights.commands.CommandInsights;
import dev.frankheijden.insights.commands.CommandScan;
import dev.frankheijden.insights.commands.CommandScanCache;
import dev.frankheijden.insights.commands.CommandScanHistory;
import dev.frankheijden.insights.commands.CommandScanRegion;
import dev.frankheijden.insights.commands.CommandScanWorld;
import dev.frankheijden.insights.commands.CommandTeleportChunk;
import dev.frankheijden.insights.commands.brigadier.BrigadierHandler;
import dev.frankheijden.insights.commands.parser.LimitArgument;
import dev.frankheijden.insights.commands.parser.ScanHistoryPageArgument;
import dev.frankheijden.insights.commands.parser.ScanObjectArrayArgument;
import dev.frankheijden.insights.commands.parser.WorldArgument;
import dev.frankheijden.insights.concurrent.ContainerExecutorService;
import dev.frankheijden.insights.listeners.manager.ListenerManager;
import dev.frankheijden.insights.nms.core.InsightsNMS;
import dev.frankheijden.insights.nms.core.InsightsNMSVersion;
import dev.frankheijden.insights.placeholders.InsightsPlaceholderExpansion;
import dev.frankheijden.insights.tasks.EntityTrackerTask;
import dev.frankheijden.insights.tasks.PlayerTrackerTask;
import io.leangen.geantyref.TypeToken;
import io.papermc.lib.PaperLib;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitTask;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

public class Insights extends InsightsPlugin {

    private static final String SETTINGS_FILE_NAME = "config.yml";
    private static final String MESSAGES_FILE_NAME = "messages.yml";
    private static final String LIMITS_FOLDER_NAME = "limits";

    private Settings settings;
    private Messages messages = null;
    private Notifications notifications;
    private Limits limits;
    private AddonManager addonManager;
    private ContainerExecutorService executor;
    private ChunkContainerExecutor chunkContainerExecutor;
    private PlayerList playerList;
    private WorldStorage worldStorage;
    private AddonStorage addonStorage;
    private WorldChunkScanTracker worldChunkScanTracker;
    private AddonScanTracker addonScanTracker;
    private EntityTrackerTask entityTrackerTask;
    private MetricsManager metricsManager;
    private ScanHistory scanHistory;
    private ListenerManager listenerManager;
    private InsightsPlaceholderExpansion placeholderExpansion;
    private BukkitTask playerTracker = null;
    private BukkitTask updateChecker = null;
    private BukkitAudiences audiences = null;
    private RedstoneUpdateCount redstoneUpdateCount = null;
    private ChunkTeleport chunkTeleport;
    private InsightsNMS nms;

    @Override
    public void onLoad() {
        super.onLoad();
        instance = this;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (isMin(19)) {
            if (isMin(19, 3)) {
                if (PaperLib.isPaper()) {
                    nms = InsightsNMS.get(InsightsNMSVersion.v1_19_4_R3);
                }
            } else if (isMin(19, 2)) {
                if (PaperLib.isPaper()) {
                    nms = InsightsNMS.get(InsightsNMSVersion.v1_19_3_R2);
                }
            } else if (isMin(19, 1) && PaperLib.isPaper()) {
                nms = InsightsNMS.get(InsightsNMSVersion.v1_19_2_R1);
            } else {
                nms = InsightsNMS.get(InsightsNMSVersion.v1_19_1_R1);
            }
        }
        if (isMin(20) && PaperLib.isPaper()) {
            if (isMin(20, 1)) {
                nms = InsightsNMS.get(InsightsNMSVersion.v1_20_R2);
            } else {
                nms = InsightsNMS.get(InsightsNMSVersion.v1_20_R1);
            }
        }
        if (nms == null) {
            throw new RuntimeException("Insights is incompatible with your server version");
        }

        this.audiences = BukkitAudiences.create(this);
        this.listenerManager = new ListenerManager(this);
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
        executor = ContainerExecutorService.newExecutor(
                settings.SCANS_CONCURRENT_THREADS,
                settings.SCANS_TIMEOUT_MILLIS
        );
        chunkContainerExecutor = new ChunkContainerExecutor(nms, executor, worldStorage, worldChunkScanTracker);
        metricsManager = new MetricsManager(this);
        scanHistory = new ScanHistory();
        redstoneUpdateCount = new RedstoneUpdateCount(this);
        redstoneUpdateCount.start();
        chunkTeleport = new ChunkTeleport(this);

        loadCommands();

        if (!PaperLib.isPaper()) {
            entityTrackerTask = new EntityTrackerTask(this);
            var interval = settings.SPIGOT_ENTITY_TRACKER_INTERVAL_TICKS;
            Bukkit.getScheduler().runTaskTimer(this, entityTrackerTask, 0, interval);
        }

        reload();
    }

    @Override
    public void onDisable() {
        listenerManager.unregister();
        redstoneUpdateCount.stop();
        notifications.clearNotifications();
        if (placeholderExpansion != null) {
            placeholderExpansion.unregister();
            placeholderExpansion = null;
        }
        chunkContainerExecutor.shutdown();
        audiences.close();
    }

    @Override
    public ListenerManager getListenerManager() {
        return listenerManager;
    }

    @Override
    public RedstoneUpdateCount getRedstoneUpdateCount() {
        return redstoneUpdateCount;
    }

    @Override
    public ChunkTeleport getChunkTeleport() {
        return chunkTeleport;
    }

    @Override
    public InsightsNMS getNMS() {
        return nms;
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
            messages = Messages.load(this, this.audiences, file, getResource(MESSAGES_FILE_NAME));
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
                IOUtils.copyResources(limitsPath, getClassLoader(), Arrays.asList(
                        "bed-limit.yml",
                        "redstone-limit.yml",
                        "tile-limit.yml"
                ));
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
                    getLogger().info("Loaded limit '" + fileName + "'");
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
        ParserRegistry<CommandSender> parserRegistry = commandManager.parserRegistry();
        parserRegistry.registerParserSupplier(
                TypeToken.get(new TypeToken<Limit>() {
                }.getType()),
                options -> new LimitArgument.LimitParser()
        );
        parserRegistry.registerParserSupplier(
                TypeToken.get(new TypeToken<ScanObject<?>[]>() {
                }.getType()),
                options -> new ScanObjectArrayArgument.ScanObjectArrayParser()
        );
        parserRegistry.registerParserSupplier(
                TypeToken.get(new TypeToken<CommandScanHistory.Page>() {
                }.getType()),
                options -> new ScanHistoryPageArgument.ScanHistoryPageParser()
        );
        parserRegistry.registerParserSupplier(
                TypeToken.get(new TypeToken<World>() {
                }.getType()),
                options -> new WorldArgument.WorldParser()
        );

        // Register capabilities if allowed
        boolean hasBrigadier = commandManager.hasCapability(CloudBukkitCapabilities.BRIGADIER);
        boolean hasNativeBrigadier = commandManager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER);
        boolean hasCommodoreBrigadier = commandManager.hasCapability(CloudBukkitCapabilities.COMMODORE_BRIGADIER);
        if (hasBrigadier && (hasNativeBrigadier || hasCommodoreBrigadier)) {
            commandManager.registerBrigadier();
            CloudBrigadierManager<CommandSender, ?> brigadierManager = commandManager.brigadierManager();
            var handler = new BrigadierHandler(brigadierManager);
            handler.registerTypes();
        }
        if (commandManager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
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
        annotationParser.parse(new CommandScanCache(this));
        annotationParser.parse(new CommandScanWorld(this));
        annotationParser.parse(new CommandScanRegion(this));
        annotationParser.parse(new CommandScanHistory(this));
        annotationParser.parse(new CommandTeleportChunk(this));
        annotationParser.parse(new CommandCancelScan(this));
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
    public ContainerExecutorService getExecutor() {
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
    public MetricsManager getMetricsManager() {
        return metricsManager;
    }

    @Override
    public ScanHistory getScanHistory() {
        return scanHistory;
    }

    @Override
    public void reload() {
        if (playerTracker != null) {
            playerTracker.cancel();
        }

        if (settings.CHUNK_SCANS_MODE == Settings.ChunkScanMode.ALWAYS) {
            playerTracker = getServer().getScheduler().runTaskTimerAsynchronously(
                    this,
                    new PlayerTrackerTask(this),
                    0,
                    settings.CHUNK_SCANS_PLAYER_TRACKER_INTERVAL_TICKS
            );
        }

        if (updateChecker != null) {
            updateChecker.cancel();
        }

        if (settings.UPDATE_CHECKER_ENABLED) {
            updateChecker = getServer().getScheduler().runTaskTimerAsynchronously(
                    this,
                    new UpdateCheckerTask(this),
                    20,
                    20L * settings.UPDATE_CHECKER_INTERVAL_SECONDS
            );
        }

        listenerManager.unregister();
        listenerManager.register();

        if (placeholderExpansion == null && isAvailable("PlaceholderAPI")) {
            placeholderExpansion = new InsightsPlaceholderExpansion(this);
            placeholderExpansion.register();
        }
    }
}
