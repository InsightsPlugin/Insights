package net.frankheijden.insights;

import io.papermc.lib.PaperLib;
import net.frankheijden.insights.commands.*;
import net.frankheijden.insights.config.Config;
import net.frankheijden.insights.listeners.*;
import net.frankheijden.insights.managers.*;
import net.frankheijden.insights.placeholders.InsightsPlaceholderAPIExpansion;
import net.frankheijden.insights.utils.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.text.NumberFormat;
import java.util.Objects;

public class Insights extends JavaPlugin {
    private static Insights insights;

    private FileConfiguration messages;

    private Config config;
    private SQLite sqLite;

    private NMSManager nmsManager = null;
    private BossBarManager bossBarManager = null;
    private WorldGuardManager worldGuardManager = null;
    private HookManager hookManager = null;
    private ScanManager scanManager = null;
    private SelectionManager selectionManager = null;

    private boolean placeholderAPIHook = false;

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();
        insights = this;

        PaperLib.suggestPaper(this);

        setupConfiguration();
        setupSQLite();
        setupManagers();
        setupClasses();
        setupPlaceholderAPIHook();

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
        InteractListener interactListener = new InteractListener();
        Bukkit.getPluginManager().registerEvents(interactListener, this);
        MainListener mainListener = new MainListener(interactListener);
        Bukkit.getPluginManager().registerEvents(mainListener, this);
        Bukkit.getPluginManager().registerEvents(new EntityListener(mainListener), this);
        if (nmsManager.isPost1_13()) {
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
        Objects.requireNonNull(this.getCommand("selection")).setExecutor(new CommandSelection());
        Objects.requireNonNull(this.getCommand("togglecheck")).setExecutor(new CommandTogglecheck());
        Objects.requireNonNull(this.getCommand("cancelscan")).setExecutor(new CommandCancelscan());
    }

    private boolean isAvailable(String pluginName) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        return plugin != null && plugin.isEnabled();
    }

    private void setupManagers() {
        nmsManager = new NMSManager();

        if (nmsManager.isPost1_9()) {
            bossBarManager = new BossBarManager();
            bossBarManager.start();
        }

        hookManager = new HookManager();

        if (isAvailable("WorldGuard")) {
            worldGuardManager = new WorldGuardManager();
            Bukkit.getLogger().info("[Insights] Successfully hooked into WorldGuard!");
        }

        scanManager = new ScanManager();
        selectionManager = new SelectionManager();

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

    public void reload() {
        setupConfiguration();
        setupSQLite();
        setupPlaceholderAPIHook();
    }

    public FileConfiguration getMessages() {
        return messages;
    }

    public Config getConfiguration() {
        return config;
    }

    public SQLite getSqLite() {
        return sqLite;
    }

    public NMSManager getNMSManager() {
        return nmsManager;
    }

    public BossBarManager getBossBarManager() {
        return bossBarManager;
    }

    public WorldGuardManager getWorldGuardManager() {
        return worldGuardManager;
    }

    public HookManager getHookManager() {
        return hookManager;
    }

    public ScanManager getScanManager() {
        return scanManager;
    }

    public SelectionManager getSelectionManager() {
        return selectionManager;
    }

    public boolean hasPlaceholderAPIHook() {
        return placeholderAPIHook;
    }
}
