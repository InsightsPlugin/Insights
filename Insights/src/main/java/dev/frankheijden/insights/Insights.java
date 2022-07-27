package dev.frankheijden.insights;

import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.arguments.parser.ArgumentParser;
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
import dev.frankheijden.insights.api.concurrent.storage.ScanHistory;
import dev.frankheijden.insights.api.config.Limits;
import dev.frankheijden.insights.api.config.Messages;
import dev.frankheijden.insights.api.config.Notifications;
import dev.frankheijden.insights.api.config.Settings;
import dev.frankheijden.insights.api.config.limits.Limit;
import dev.frankheijden.insights.api.config.parser.YamlParseException;
import dev.frankheijden.insights.api.metrics.MetricsManager;
import dev.frankheijden.insights.api.objects.wrappers.ScanObject;
import dev.frankheijden.insights.api.region.Region;
import dev.frankheijden.insights.api.region.RegionManager;
import dev.frankheijden.insights.api.tasks.UpdateCheckerTask;
import dev.frankheijden.insights.api.util.Pair;
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
import dev.frankheijden.insights.commands.parsers.LimitParser;
import dev.frankheijden.insights.commands.parsers.RegionParser;
import dev.frankheijden.insights.commands.parsers.ScanHistoryPageParser;
import dev.frankheijden.insights.commands.parsers.ScanObjectArrayParser;
import dev.frankheijden.insights.commands.parsers.WorldParser;
import dev.frankheijden.insights.concurrent.ContainerExecutorService;
import dev.frankheijden.insights.listeners.manager.ListenerManager;
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
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
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
    private RegionManager regionManager;
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

    @Override
    public void onLoad() {
        super.onLoad();
        instance = this;
    }

    @Override
    public void onEnable() {
        super.onEnable();
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
                addonManager.registerAddons();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }, 1);

        playerList = new PlayerList(Bukkit.getOnlinePlayers());
        regionManager = new RegionManager(this);
        executor = ContainerExecutorService.newExecutor(settings.SCANS_CONCURRENT_THREADS);
        chunkContainerExecutor = new ChunkContainerExecutor(this);
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
        addonManager.unregisterAddons();
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
    public @NonNull ListenerManager listenerManager() {
        return listenerManager;
    }

    @Override
    public @NonNull RedstoneUpdateCount redstoneUpdateCount() {
        return redstoneUpdateCount;
    }

    @Override
    public @NonNull ChunkTeleport chunkTeleport() {
        return chunkTeleport;
    }

    public @Nullable EntityTrackerTask entityTracker() {
        return entityTrackerTask;
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
        limits = new Limits(this);

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
        List.<Pair<TypeToken<?>, ArgumentParser<CommandSender, ?>>>of(
                new Pair<>(new TypeToken<Limit>() {}, new LimitParser()),
                new Pair<>(new TypeToken<Region>() {}, new RegionParser()),
                new Pair<>(new TypeToken<CommandScanHistory.Page>() {}, new ScanHistoryPageParser()),
                new Pair<>(new TypeToken<ScanObject<?>[]>() {}, new ScanObjectArrayParser()),
                new Pair<>(new TypeToken<World>() {}, new WorldParser())
        ).forEach(p -> parserRegistry.registerParserSupplier(TypeToken.get(p.a().getType()), options -> p.b()));

        // Register capabilities if allowed
        if (commandManager.hasCapability(CloudBukkitCapabilities.BRIGADIER)) {
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
        List.of(
                new CommandInsights(this),
                new CommandScan(this),
                new CommandScanCache(this),
                new CommandScanWorld(this),
                new CommandScanRegion(this),
                new CommandScanHistory(this),
                new CommandTeleportChunk(this),
                new CommandCancelScan(this)
        ).forEach(annotationParser::parse);
    }

    @Override
    public @NonNull Settings settings() {
        return settings;
    }

    @Override
    public @NonNull Messages messages() {
        return messages;
    }

    @Override
    public @NonNull Limits limits() {
        return limits;
    }

    @Override
    public @NonNull AddonManager addonManager() {
        return addonManager;
    }

    @Override
    public @NonNull Notifications notifications() {
        return notifications;
    }

    @Override
    public @NonNull ContainerExecutorService executor() {
        return executor;
    }

    @Override
    public @NonNull ChunkContainerExecutor chunkContainerExecutor() {
        return chunkContainerExecutor;
    }

    @Override
    public @NonNull PlayerList playerList() {
        return playerList;
    }

    @Override
    public @NonNull RegionManager regionManager() {
        return regionManager;
    }

    @Override
    public @NonNull MetricsManager metricsManager() {
        return metricsManager;
    }

    public @NonNull ScanHistory scanHistory() {
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
