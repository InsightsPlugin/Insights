package net.frankheijden.insights;

import io.papermc.lib.PaperLib;
import net.frankheijden.insights.commands.*;
import net.frankheijden.insights.config.Config;
import net.frankheijden.insights.events.ScanCompleteEvent;
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

    private FileConfiguration messages;

    public static String NMS;
    private boolean post1_8_R1 = false;
    private boolean post1_9 = false;
    private boolean post1_13 = false;

    private Config config;
    private SQLite sqLite;
    private BossBarUtils bossBarUtils;
    private WorldGuardUtils worldGuardUtils = null;

    private HookManager hookManager;
    private boolean placeholderAPIHook = false;

    private final List<LoadChunksTask> scanTasks = new ArrayList<>();

    private final Map<UUID, LoadChunksTask> playerScanTasks = new HashMap<>();
    private boolean consoleScanning = false;
    private final Map<String, ScanCompleteEvent> countsMap = new HashMap<>();

    private String versionQueued = null;
    private boolean download = false;
    private final List<Player> notifyPlayers = new ArrayList<>();

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();
        insights = this;

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

    public static Insights getInstance() {
        return insights;
    }

    private void setupConfiguration() {
        config = new Config();
        config.reload();

        File messagesFile = FileUtils.copyResourceIfNotExists("messages.yml");
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    private void setupSQLite() {
        sqLite = new SQLite();
        sqLite.load();
    }

    private void setupClasses() {
        if (isAvailable("WorldGuard")) {
            worldGuardUtils = new WorldGuardUtils();
            Bukkit.getLogger().info("[Insights] Successfully hooked into WorldGuard!");
        }

        InteractListener interactListener = new InteractListener();
        Bukkit.getPluginManager().registerEvents(interactListener, this);
        MainListener mainListener = new MainListener(interactListener);
        Bukkit.getPluginManager().registerEvents(mainListener, this);
        Bukkit.getPluginManager().registerEvents(new EntityListener(mainListener), this);
        if (post1_13) {
            Bukkit.getPluginManager().registerEvents(new Post1_13Listeners(), this);
        } else {
            Bukkit.getPluginManager().registerEvents(new Pre1_13Listeners(mainListener), this);
        }
        Objects.requireNonNull(this.getCommand("autoscan")).setExecutor(new CommandAutoscan());
        Objects.requireNonNull(this.getCommand("insights")).setExecutor(new CommandInsights());
        Objects.requireNonNull(this.getCommand("check")).setExecutor(new CommandCheck());
        Objects.requireNonNull(this.getCommand("checkworlds")).setExecutor(new CommandCheckworlds());
        Objects.requireNonNull(this.getCommand("scan")).setExecutor(new CommandScan());
        Objects.requireNonNull(this.getCommand("scanradius")).setExecutor(new CommandScanradius());
        Objects.requireNonNull(this.getCommand("scanworld")).setExecutor(new CommandScanworld());
        Objects.requireNonNull(this.getCommand("togglecheck")).setExecutor(new CommandTogglecheck());
        Objects.requireNonNull(this.getCommand("cancelscan")).setExecutor(new CommandCancelscan());
    }

    private boolean isAvailable(String pluginName) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        return plugin != null && plugin.isEnabled();
    }

    private void setupNMS() {
        NMS = Bukkit.getServer().getClass().getPackage().getName();
        NMS = NMS.substring(NMS.lastIndexOf(".") + 1);

        if (PaperLib.getMinecraftVersion() >= 8 && !NMS.equalsIgnoreCase("v1_8_R1")) {
            post1_8_R1 = true;
        }

        if (PaperLib.getMinecraftVersion() >= 9) {
            bossBarUtils = new BossBarUtils();
            bossBarUtils.setupBossBarUtils();
            bossBarUtils.setupBossBarRunnable();
            post1_9 = true;
        }

        if (PaperLib.getMinecraftVersion() >= 13) {
            post1_13 = true;
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
            InsightsPlaceholderAPIExpansion expansion = new InsightsPlaceholderAPIExpansion();
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
        hookManager = new HookManager();
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

    public FileConfiguration getMessages() {
        return messages;
    }

    public boolean isPost1_8_R1() {
        return post1_8_R1;
    }

    public boolean isPost1_9() {
        return post1_9;
    }

    public boolean isPost1_13() {
        return post1_13;
    }

    public Config getConfiguration() {
        return config;
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
            if (sendMessage) MessageUtils.sendMessage(player, "messages.already_scanning");
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
