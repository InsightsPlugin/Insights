package dev.frankheijden.insights;

import com.github.zafarkhaja.semver.Version;
import com.mojang.brigadier.arguments.StringArgumentType;
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
import dev.frankheijden.insights.commands.parser.LimitParser;
import dev.frankheijden.insights.commands.parser.ScanHistoryPageParser;
import dev.frankheijden.insights.commands.parser.ScanObjectArrayParser;
import dev.frankheijden.insights.commands.parser.WorldParser;
import dev.frankheijden.insights.commands.util.CommandSenderMapper;
import dev.frankheijden.insights.concurrent.ContainerExecutorService;
import dev.frankheijden.insights.listeners.manager.ListenerManager;
import dev.frankheijden.insights.nms.core.InsightsNMS;
import dev.frankheijden.insights.placeholders.InsightsPlaceholderExpansion;
import dev.frankheijden.insights.tasks.PlayerTrackerTask;
import io.leangen.geantyref.TypeToken;
import io.papermc.lib.PaperLib;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitTask;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.PaperCommandManager;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;

public class Insights extends InsightsPlugin {

    private static final Version minimumCompatibleVersion = Version.of(1, 20, 6);

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

        if (isIncompatible()) {
            throw new RuntimeException("Insights is incompatible with your server version, "
                    + "we require a Paper backend and a Minecraft version of at least " + minimumCompatibleVersion);
        }
        nms = InsightsNMS.get();

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

        reload();
    }

    private static boolean isIncompatible() {
        var minecraftVersion = Version.parse(Bukkit.getServer().getMinecraftVersion(), false);
        return !PaperLib.isPaper() || minecraftVersion.compareTo(minimumCompatibleVersion) < 0;
    }

    @Override
    public void onDisable() {
        if (isIncompatible()) return;

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
        var commandManager = PaperCommandManager.builder(new CommandSenderMapper())
                .executionCoordinator(ExecutionCoordinator.asyncCoordinator())
                .buildOnEnable(this);

        // Register parsers
        var parserRegistry = commandManager.parserRegistry();
        parserRegistry.registerParserSupplier(
                TypeToken.get(new TypeToken<Limit>() {
                }.getType()),
                LimitParser::new
        );
        parserRegistry.registerParserSupplier(
                TypeToken.get(new TypeToken<ScanObject<?>[]>() {
                }.getType()),
                ScanObjectArrayParser::new
        );
        parserRegistry.registerParserSupplier(
                TypeToken.get(new TypeToken<CommandScanHistory.Page>() {
                }.getType()),
                ScanHistoryPageParser::new
        );
        parserRegistry.registerParserSupplier(
                TypeToken.get(new TypeToken<World>() {
                }.getType()),
                WorldParser::new
        );

        if (commandManager.hasBrigadierManager()) {
            commandManager.brigadierManager().registerMapping(
                    new TypeToken<ScanObjectArrayParser<CommandSender>>() {},
                    builder -> {
                        builder.to(argument -> StringArgumentType.greedyString());
                        builder.cloudSuggestions();
                    }
            );
        }

        // Create Annotation Parser
        var annotationParser = new AnnotationParser(commandManager, CommandSender.class);

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
