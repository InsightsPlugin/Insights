package net.frankheijden.insights;

import io.papermc.lib.PaperLib;
import net.frankheijden.insights.api.InsightsAPI;
import net.frankheijden.insights.api.events.ScanCompleteEvent;
import net.frankheijden.insights.commands.*;
import net.frankheijden.insights.config.Config;
import net.frankheijden.insights.hooks.HookManager;
import net.frankheijden.insights.listeners.*;
import net.frankheijden.insights.placeholders.InsightsPlaceholderAPIExpansion;
import net.frankheijden.insights.tasks.LoadChunksTask;
import net.frankheijden.insights.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.text.NumberFormat;
import java.util.*;

public class Insights extends JavaPlugin {
    private static Insights insights;
    public static Insights getInstance() {
        return insights;
    }

    public Insights(){}

    private FileConfiguration messages;

    private String nms;
    private boolean oldActionBar = false;
    private boolean newAPI = false;

    private Config config;
    private Utils utils;
    private SQLite sqLite;
    private BossBarUtils bossBarUtils;
    private WorldGuardUtils worldGuardUtils = null;

    private HookManager hookManager;
    private boolean placeholderAPIHook = false;

    private List<LoadChunksTask> scanTasks = new ArrayList<>();

    private Map<UUID, LoadChunksTask> playerScanTasks = new HashMap<>();
    private boolean consoleScanning = false;
    private Map<String, ScanCompleteEvent> countsMap = new HashMap<>();

    private String versionQueued = null;
    private boolean download = false;
    private List<Player> notifyPlayers = new ArrayList<>();

    private InsightsAPI insightsAPI;

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();
        insights = this;
        insightsAPI = new InsightsAPI();

        PaperLib.suggestPaper(this);

        setupConfiguration();
        setupSQLite();
        setupNMS();
        setupClasses();
        setupPlaceholderAPIHook();
        setupPluginHooks();

        long end = System.currentTimeMillis();
        long millis = end - start;
        Bukkit.getLogger().info("[Insights] Enabled Insights in "
                + NumberFormat.getInstance().format(millis) + "ms!");
    }

    private void setupConfiguration() {
        config = new Config(this);
        config.reload();

        File messagesFile = FileUtils.copyResourceIfNotExists("messages.yml");
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    private void setupSQLite() {
        sqLite = new SQLite(this);
        sqLite.load();
    }

    private void setupClasses() {
        utils = new Utils(this);
        if (isAvailable("WorldGuard")) {
            worldGuardUtils = new WorldGuardUtils(this);
            Bukkit.getLogger().info("[Insights] Successfully hooked into WorldGuard!");
        }

        InteractListener interactListener = new InteractListener();
        Bukkit.getPluginManager().registerEvents(interactListener, this);
        MainListener mainListener = new MainListener(this, interactListener);
        Bukkit.getPluginManager().registerEvents(mainListener, this);
        if (newAPI) {
            Bukkit.getPluginManager().registerEvents(new Post1_13Listeners(mainListener), this);
        } else {
            Bukkit.getPluginManager().registerEvents(new Pre1_13Listeners(mainListener), this);
        }
        Objects.requireNonNull(this.getCommand("autoscan")).setExecutor(new CommandAutoscan(this));
        Objects.requireNonNull(this.getCommand("insights")).setExecutor(new CommandInsights(this));
        Objects.requireNonNull(this.getCommand("check")).setExecutor(new CommandCheck(this));
        Objects.requireNonNull(this.getCommand("checkworlds")).setExecutor(new CommandCheckworlds(this));
        Objects.requireNonNull(this.getCommand("scan")).setExecutor(new CommandScan(this));
        Objects.requireNonNull(this.getCommand("scanradius")).setExecutor(new CommandScanradius(this));
        Objects.requireNonNull(this.getCommand("scanworld")).setExecutor(new CommandScanworld(this));
        Objects.requireNonNull(this.getCommand("togglecheck")).setExecutor(new CommandTogglecheck(this));
        Objects.requireNonNull(this.getCommand("cancelscan")).setExecutor(new CommandCancelscan(this));
    }

    private boolean isAvailable(String pluginName) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        return plugin != null && plugin.isEnabled();
    }

    private void setupNMS() {
        nms = Bukkit.getServer().getClass().getPackage().getName();
        nms = nms.substring(nms.lastIndexOf(".") + 1);

        if (PaperLib.getMinecraftVersion() <= 8) {
            oldActionBar = true;
        }

        if (PaperLib.getMinecraftVersion() >= 9) {
            bossBarUtils = new BossBarUtils(this);
            bossBarUtils.setupBossBarUtils();
            bossBarUtils.setupBossBarRunnable();
        }

        if (PaperLib.getMinecraftVersion() >= 13) {
            newAPI = true;
        }

        String version = String.format("1.%d.%d", PaperLib.getMinecraftVersion(), PaperLib.getMinecraftPatchVersion());
        if (PaperLib.getMinecraftVersion() <= 7) {
            Bukkit.getLogger().warning("[Insights] Minecraft version '" + version + "' detected, "
                    + "please note that Insights may not support this version!");
        } else {
            Bukkit.getLogger().info("[Insights] Minecraft version '" + version + "' detected!");
        }
    }

    private void setupPlaceholderAPIHook() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            InsightsPlaceholderAPIExpansion expansion = new InsightsPlaceholderAPIExpansion(this);
            if (expansion.register()) {
                Bukkit.getLogger().info("[Insights] Successfully hooked into PlaceholderAPI!");
            }

            placeholderAPIHook = expansion.isRegistered();
            if (!placeholderAPIHook) {
                Bukkit.getLogger().warning("[Insights] Couldn't hook into PlaceholderAPI.");
            }
        }
    }

    private void setupPluginHooks() {
        hookManager = new HookManager(this);
    }

    public void addScanTask(LoadChunksTask loadChunksTask) {
        scanTasks.add(loadChunksTask);
    }

    public int getTaskID(LoadChunksTask loadChunksTask) {
        if (scanTasks.contains(loadChunksTask)) {
            return scanTasks.indexOf(loadChunksTask) + 1;
        }
        return -1;
    }

    public enum LogType {
        INFO,
        WARNING,
        DEBUG
    }

    public void log(LogType logType, String message) {
        log(logType, message, null);
    }

    public void log(LogType logType, String message, Integer taskID) {
        if (logType == LogType.DEBUG && !config.GENERAL_DEBUG) {
            return;
        }
        Bukkit.getLogger().info("[Insights] [" + logType.name() + "] " + ((taskID != null) ? ("[TASK #" + taskID + "] ") : "") + message);
    }

    public void reload() {
        setupConfiguration();
        setupSQLite();
        setupPlaceholderAPIHook();

        if (PaperLib.getMinecraftVersion() >= 9) {
            bossBarUtils.setupBossBarUtils();
        }
    }

    public InsightsAPI getInsightsAPI() {
        return insightsAPI;
    }

    public FileConfiguration getMessages() {
        return messages;
    }

    public String getNms() {
        return nms;
    }

    public boolean shouldUseOldActionBar() {
        return oldActionBar;
    }

    public boolean shouldUseNewAPI() {
        return newAPI;
    }

    public Config getConfiguration() {
        return config;
    }

    public Utils getUtils() {
        return utils;
    }

    public SQLite getSqLite() {
        return sqLite;
    }

    public BossBarUtils getBossBarUtils() {
        return bossBarUtils;
    }

    public WorldGuardUtils getWorldGuardUtils() {
        return worldGuardUtils;
    }

    public HookManager getHookManager() {
        return hookManager;
    }

    public boolean hasPlaceholderAPIHook() {
        return placeholderAPIHook;
    }

    public Map<UUID, LoadChunksTask> getPlayerScanTasks() {
        return playerScanTasks;
    }

    public boolean isPlayerScanning(Player player, boolean sendMessage) {
        if (playerScanTasks.containsKey(player.getUniqueId())) {
            if (sendMessage) utils.sendMessage(player, "messages.already_scanning");
            return true;
        }
        return false;
    }

    public Map<String, ScanCompleteEvent> getCountsMap() {
        return countsMap;
    }

    public boolean isConsoleScanning() {
        return consoleScanning;
    }

    public void setConsoleScanning(boolean consoleScanning) {
        this.consoleScanning = consoleScanning;
    }

    public String getVersionQueued() {
        return versionQueued;
    }

    public void setVersionQueued(String versionQueued) {
        this.versionQueued = versionQueued;
    }

    public boolean isDownloading() {
        return download;
    }

    public void setDownloading(boolean download) {
        this.download = download;
    }

    public void addNotifyPlayer(Player player) {
        this.notifyPlayers.add(player);
    }

    public List<Player> getNotifyPlayers() {
        return notifyPlayers;
    }

    public void clearNotifyPlayers() {
        this.notifyPlayers.clear();
    }
}
