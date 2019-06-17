package net.frankheijden.insights;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

public class Config {
    private Insights plugin;
    private YamlConfiguration config;

    public int GENERAL_LIMIT = 256;
    public List<String> GENERAL_WORLDS = Arrays.asList("world", "world_nether", "world_the_end");
    public String GENERAL_NOTIFICATION_TYPE = "BOSSBAR";
    private List<String> GENERAL_NOTIFICATION_TYPE_VALUES = Arrays.asList("BOSSBAR", "ACTIONBAR");
    public String GENERAL_NOTIFICATION_BOSSBAR_COLOR = "BLUE";
    private List<String> GENERAL_NOTIFICATION_BOSSBAR_COLOR_VALUES = Arrays.asList("BLUE", "GREEN", "PINK", "PURPLE", "RED", "WHITE", "YELLOW");
    public String GENERAL_NOTIFICATION_BOSSBAR_STYLE = "SEGMENTED_10";
    private List<String> GENERAL_NOTIFICATION_BOSSBAR_STYLE_VALUES = Arrays.asList("SOLID", "SEGMENTED_6", "SEGMENTED_10", "SEGMENTED_12", "SEGMENTED_20");
    public List<String> GENERAL_NOTIFICATION_BOSSBAR_FLAGS = Collections.emptyList();
    private List<String> GENERAL_NOTIFICATION_BOSSBAR_FLAGS_VALUES = Arrays.asList("DARKEN_SKY", "PLAY_BOSS_MUSIC", "CREATE_FOG");
    public int GENERAL_NOTIFICATION_BOSSBAR_DURATION = 60;
    public int GENERAL_SCANRADIUS_DEFAULT = 5;
    public boolean GENERAL_SCAN_NOTIFICATION = true;
    public Map<Material, Integer> GENERAL_MATERIALS;

    public Config(Insights plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            Bukkit.getLogger().info("[Insights] config.yml not found, creating!");
            plugin.saveDefaultConfig();
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        int generalLimit = config.getInt("general.limit");
        if (generalLimit > -1) {
            GENERAL_LIMIT = generalLimit;
        } else {
            System.out.println("[Insights/Config] Chunk tile limit was chosen not to be enabled.");
        }
        GENERAL_WORLDS = config.getStringList("general.worlds");

        updateString("general.notification.type", GENERAL_NOTIFICATION_TYPE_VALUES);
        updateString("general.notification.bossbar.color", GENERAL_NOTIFICATION_BOSSBAR_COLOR_VALUES);
        updateString("general.notification.bossbar.style", GENERAL_NOTIFICATION_BOSSBAR_STYLE_VALUES);

        List<String> bossbarFlags = config.getStringList("general.notification.bossbar.flags");
        for (String flag : bossbarFlags) {
            if (!GENERAL_NOTIFICATION_BOSSBAR_FLAGS_VALUES.contains(flag.toUpperCase())) {
                bossbarFlags.remove(flag);
            }
        }
        GENERAL_NOTIFICATION_BOSSBAR_FLAGS = bossbarFlags;

        updateInt("general.notification.bossbar.duration", 1);
        updateInt("general.scanradius_default", 1);

        GENERAL_SCAN_NOTIFICATION = config.getBoolean("general.scan_notification");

        GENERAL_MATERIALS = new HashMap<>();
        MemorySection materials = (MemorySection) config.get("general.materials");
        if (materials != null) {
            for (String materialString : materials.getKeys(false)) {
                Material material;
                try {
                    material = Material.valueOf(materialString);
                } catch (Exception ex) {
                    System.err.println("[Insights/Config] Invalid configuration in config.yml at path 'general.materials', invalid material '" + materialString + "'!");
                    continue;
                }

                int value = materials.getInt(materialString);
                if (value >= 0) {
                    GENERAL_MATERIALS.put(material, value);
                } else {
                    System.err.println("[Insights/Config] Invalid configuration in config.yml at path 'general.materials." + materialString + "', value must be at least 0!");
                }
            }
        } else {
            System.err.println("[Insights/Config] Configuration section in config.yml not found at path 'general.materials'!");
        }
    }

    private void updateInt(String path, int min) {
        String fieldName = path.replace(".", "_").toUpperCase();
        int i = config.getInt(path);
        if (i >= min) {
            try {
                Class<?> configClass = this.getClass();
                Field field = configClass.getDeclaredField(fieldName);
                field.set(this, i);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            System.err.println("[Insights/Config] Invalid configuration in config.yml at path '" + path + "', value must be at least " + min + "!");
        }
    }

    private void updateString(String path, List<String> possibleValues) {
        String fieldName = path.replace(".", "_").toUpperCase();
        String s = config.getString(path);
        if (s != null && possibleValues.contains(s.toUpperCase())) {
            try {
                Class<?> configClass = this.getClass();
                Field field = configClass.getDeclaredField(fieldName);
                field.set(this, s);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            StringBuilder sb = new StringBuilder();
            for (String value : possibleValues) {
                if (possibleValues.indexOf(value) + 1 == possibleValues.size()) {
                    sb.append(" or \"").append(value).append("\"");
                } else {
                    sb.append("\"").append(value).append("\", ");
                }
            }
            System.err.println("[Insights/Config] Invalid configuration in config.yml at path '" + path + "', value must be " + sb.toString() + "!");
        }
    }
}
