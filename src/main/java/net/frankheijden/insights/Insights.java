package net.frankheijden.insights;

import io.papermc.lib.PaperLib;
import net.frankheijden.insights.commands.*;
import net.frankheijden.insights.utils.BossBarUtils;
import net.frankheijden.insights.utils.Utils;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

public class Insights extends JavaPlugin {
    public FileConfiguration messages;

    public Insights(){}

    public String nms;
    public boolean oldActionBar = false;
    public boolean useNewAPI = true;

    public Config config;
    public Utils utils;
    public SQLite sqLite;
    public BossBarUtils bossBarUtils;

    public HashMap<String, HashMap<Material, Integer>> chunkSnapshotHashMap = new HashMap<>();

    @Override
    public void onEnable() {
        PaperLib.suggestPaper(this);

        setupConfiguration();
        setupClasses();
        setupNMS();

        if (PaperLib.getMinecraftVersion() >= 9) {
            bossBarUtils = new BossBarUtils(this);
            bossBarUtils.setupDefaultBossBar();
            bossBarUtils.setupBossBarRunnable();
        }
    }

    private void setupConfiguration() {
        config = new Config(this);
        config.reload();

        File messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            Bukkit.getLogger().info("[Insights] messages.yml not found, creating!");
            saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    private void setupClasses() {
        utils = new Utils(this);
        sqLite = new SQLite(this);
        sqLite.load();

        Bukkit.getPluginManager().registerEvents(new Listeners(this), this);
        Objects.requireNonNull(this.getCommand("insights")).setExecutor(new CommandInsights(this));
        Objects.requireNonNull(this.getCommand("check")).setExecutor(new CommandCheck(this));
        Objects.requireNonNull(this.getCommand("checkworlds")).setExecutor(new CommandCheckworlds(this));
        Objects.requireNonNull(this.getCommand("scan")).setExecutor(new CommandScan(this));
        Objects.requireNonNull(this.getCommand("scanradius")).setExecutor(new CommandScanradius(this));
        Objects.requireNonNull(this.getCommand("scanworld")).setExecutor(new CommandScanworld(this));
        Objects.requireNonNull(this.getCommand("togglecheck")).setExecutor(new CommandTogglecheck(this));
    }

    private void setupNMS() {
        nms = Bukkit.getServer().getClass().getPackage().getName();
        nms = nms.substring(nms.lastIndexOf(".") + 1);
        if (nms.equalsIgnoreCase("v1_8_R1") || nms.startsWith("v1_7_")) {
            oldActionBar = true;
        }

        if (nms.startsWith("v1_12_") || nms.startsWith("v1_11_") || nms.startsWith("v1_10_") || nms.startsWith("v1_9_") || nms.startsWith("v1_8_")) {
            useNewAPI = false;
        }
        Bukkit.getLogger().info("[Insights] NMS version '"+nms+"' detected!");
    }

    public void reload() {
        setupConfiguration();

        if (PaperLib.getMinecraftVersion() >= 9) {
            bossBarUtils.setupDefaultBossBar();
        }
    }
}
