package dev.frankheijden.insights;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.concurrent.ChunkContainerExecutor;
import dev.frankheijden.insights.api.concurrent.ContainerExecutor;
import dev.frankheijden.insights.api.concurrent.PlayerList;
import dev.frankheijden.insights.api.concurrent.storage.WorldDistributionStorage;
import dev.frankheijden.insights.api.concurrent.tracker.WorldChunkScanTracker;
import dev.frankheijden.insights.api.config.Limits;
import dev.frankheijden.insights.api.config.Messages;
import dev.frankheijden.insights.api.config.Settings;
import dev.frankheijden.insights.api.config.limits.Limit;
import dev.frankheijden.insights.api.config.parser.YamlParseException;
import dev.frankheijden.insights.api.listeners.InsightsListener;
import dev.frankheijden.insights.api.utils.IOUtils;
import dev.frankheijden.insights.concurrent.ContainerExecutorService;
import dev.frankheijden.insights.listeners.ChunkListener;
import dev.frankheijden.insights.listeners.PlayerListener;
import dev.frankheijden.insights.tasks.PlayerTrackerTask;
import org.bukkit.Bukkit;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class Insights extends InsightsPlugin {

    private static final String SETTINGS_FILE_NAME = "config.yml";
    private static final String MESSAGES_FILE_NAME = "messages.yml";
    private static final String EXAMPLE_LIMITS_FOLDER_NAME = "example-limits";
    private static final String LIMITS_FOLDER_NAME = "limits";

    private static Insights instance;
    private Settings settings;
    private Messages messages;
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

    public static Insights getInstance() {
        return instance;
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

        registerEvents(
                new ChunkListener(this),
                new PlayerListener(this)
        );

        if (settings.CHUNK_SCAN_MODE == Settings.ChunkScanMode.ALWAYS) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(this, new PlayerTrackerTask(this), 0, 1);
        }
    }

    @Override
    public void reloadSettings() {
        File file = new File(getDataFolder(), SETTINGS_FILE_NAME);
        try {
            settings = Settings.load(file, getResource(SETTINGS_FILE_NAME)).exceptionally(getLogger());
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

    private void registerEvents(InsightsListener... listeners) {
        for (InsightsListener listener : listeners) {
            Bukkit.getPluginManager().registerEvents(listener, this);
        }
    }
}
