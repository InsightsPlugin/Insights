package net.frankheijden.blocklimiter;

import net.frankheijden.blocklimiter.commands.*;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

public class BlockLimiter extends JavaPlugin {
    private BlockLimiter plugin;
    public FileConfiguration config;
    public int max;

    public BlockLimiter(){}

    public String nms;
    public boolean oldActionBar = false;
    public boolean useNewAPI = true;

    public Utils utils;

    // ChunkX_ChunkZ : MaterialName : Count
    public HashMap<String, HashMap<Material, Integer>> chunkSnapshotHashMap = new HashMap<>();

    @Override
    public void onEnable() {
        this.plugin = this;

        setupConfiguration();
        setupClasses();
        setupNMS();
    }

    private void setupConfiguration() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            Bukkit.getLogger().info("[BlockLimiter] config.yml not found, creating!");
            saveDefaultConfig();
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        max = config.getInt("general.limit");
    }

    private void setupClasses() {
        utils = new Utils(this);

        Bukkit.getPluginManager().registerEvents(new Listeners(this), this);
        Objects.requireNonNull(this.getCommand("blocklimiter")).setExecutor(new CommandBlockLimiter(this));
        Objects.requireNonNull(this.getCommand("check")).setExecutor(new CommandCheck(this));
        Objects.requireNonNull(this.getCommand("checkworlds")).setExecutor(new CommandCheckworlds(this));
        Objects.requireNonNull(this.getCommand("scan")).setExecutor(new CommandScan(this));
        Objects.requireNonNull(this.getCommand("scanworld")).setExecutor(new CommandScanworld(this));
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
    }

    @Override
    public void onDisable() {
        this.plugin = null;
    }
}
