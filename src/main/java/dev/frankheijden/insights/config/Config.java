package dev.frankheijden.insights.config;

import dev.frankheijden.insights.entities.Error;
import dev.frankheijden.insights.utils.CaseInsensitiveHashSet;
import dev.frankheijden.insights.utils.FileUtils;
import dev.frankheijden.insights.utils.Utils;
import dev.frankheijden.insights.utils.YamlUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Config {

    public boolean GENERAL_UPDATES_CHECK;
    public boolean GENERAL_UPDATES_DOWNLOAD;
    public boolean GENERAL_UPDATES_DOWNLOAD_STARTUP;
    public boolean GENERAL_SUGGEST_PAPER;
    public Location GENERAL_TILEFINDER_LOCATION;
    public int GENERAL_LIMIT;
    public Map<String, Set<String>> GENERAL_IGNORE_NBT_TAGS;
    public boolean GENERAL_IGNORE_CUSTOM_SPAWN;
    public boolean GENERAL_ASYNC_ENABLED;
    public boolean GENERAL_ASYNC_WHITELIST;
    public Set<String> GENERAL_ASYNC_LIST;
    public boolean GENERAL_WORLDS_WHITELIST;
    public List<String> GENERAL_WORLDS_LIST;
    public boolean GENERAL_REGIONS_WHITELIST;
    public List<String> GENERAL_REGIONS_LIST;
    public List<RegionBlocks> GENERAL_REGION_BLOCKS;
    public boolean GENERAL_WORLDEDIT_ENABLED;
    public String GENERAL_WORLDEDIT_TYPE;
    private final Set<String> GENERAL_WORLDEDIT_TYPE_VALUES = of("UNCHANGED", "REPLACEMENT");
    public String GENERAL_WORLDEDIT_REPLACEMENT;
    public boolean GENERAL_WORLDEDIT_DISABLE_TILES;
    public boolean GENERAL_WORLDEDIT_WHITELIST;
    public Set<String> GENERAL_WORLDEDIT_LIST;
    public String GENERAL_NOTIFICATION_TYPE;
    private final Set<String> GENERAL_NOTIFICATION_TYPE_VALUES = of("BOSSBAR", "ACTIONBAR");
    public String GENERAL_NOTIFICATION_BOSSBAR_COLOR;
    private final Set<String> GENERAL_NOTIFICATION_BOSSBAR_COLOR_VALUES = of("BLUE", "GREEN", "PINK", "PURPLE", "RED", "WHITE", "YELLOW");
    public String GENERAL_NOTIFICATION_BOSSBAR_STYLE;
    private final Set<String> GENERAL_NOTIFICATION_BOSSBAR_STYLE_VALUES = of("SOLID", "SEGMENTED_6", "SEGMENTED_10", "SEGMENTED_12", "SEGMENTED_20");
    public List<String> GENERAL_NOTIFICATION_BOSSBAR_FLAGS;
    private final Set<String> GENERAL_NOTIFICATION_BOSSBAR_FLAGS_VALUES = of("DARKEN_SKY", "PLAY_BOSS_MUSIC", "CREATE_FOG");
    public int GENERAL_NOTIFICATION_BOSSBAR_DURATION;
    public List<String> GENERAL_NOTIFICATION_PASSIVE;
    public Set<String> GENERAL_NOTIFICATION_PASSIVE_VALUES = of("block", "entity", "region", "tile");
    public int GENERAL_SCANRADIUS_DEFAULT;
    public boolean GENERAL_SCAN_NOTIFICATION;
    public boolean GENERAL_ALWAYS_SHOW_NOTIFICATION;

    private final Limits limits;

    public Config() {
        this.limits = new Limits();
    }

    public void reload(List<Error> errors) {
        File configFile = FileUtils.copyResourceIfNotExists("config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        YamlUtils utils = new YamlUtils(errors, config, "config.yml");

        GENERAL_UPDATES_CHECK = utils.getBoolean("general.updates.check", true);
        GENERAL_UPDATES_DOWNLOAD = utils.getBoolean("general.updates.download", false);
        GENERAL_UPDATES_DOWNLOAD_STARTUP = utils.getBoolean("general.updates.download_startup", false);

        GENERAL_SUGGEST_PAPER = utils.getBoolean("general.suggest_paper", true);

        GENERAL_TILEFINDER_LOCATION = utils.getLocation("general.tilefinder_location", new Location(Bukkit.getWorlds().get(0), 1, 1, 1));

        GENERAL_LIMIT = utils.getIntWithinRange("general.limit", -1, null, null);

        GENERAL_IGNORE_NBT_TAGS = utils.getMapFromList("general.ignore-nbt-tags", ":");
        GENERAL_IGNORE_CUSTOM_SPAWN = utils.getBoolean("general.ignore-custom-spawn", true);

        GENERAL_ASYNC_ENABLED = utils.getBoolean("general.async.enabled", true);
        GENERAL_ASYNC_WHITELIST = utils.getBoolean("general.async.whitelist", false);
        GENERAL_ASYNC_LIST = utils.getSet("general.async.blocks", Utils.SCANNABLE_BLOCKS, "block", new CaseInsensitiveHashSet());

        GENERAL_WORLDS_WHITELIST = utils.getBoolean("general.worlds.whitelist", true);
        GENERAL_WORLDS_LIST = utils.getStringList("general.worlds.list");

        GENERAL_REGION_BLOCKS = new ArrayList<>();
        for (String regionEntry : utils.getKeys("general.region_blocks")) {
            String regionPath = YamlUtils.getPath("general.region_blocks", regionEntry);
            RegionBlocks regionBlocks = RegionBlocks.from(utils, regionPath);
            GENERAL_REGION_BLOCKS.add(regionBlocks);
        }

        GENERAL_WORLDEDIT_ENABLED = utils.getBoolean("general.worldedit.enabled", true);
        GENERAL_WORLDEDIT_TYPE = utils.getString("general.worldedit.type", "REPLACEMENT", GENERAL_WORLDEDIT_TYPE_VALUES);
        GENERAL_WORLDEDIT_REPLACEMENT = utils.getString("general.worldedit.replacement", "BEDROCK", Utils.SCANNABLE_BLOCKS, "block");
        GENERAL_WORLDEDIT_DISABLE_TILES = utils.getBoolean("general.worldedit.disable_tiles", false);
        GENERAL_WORLDEDIT_WHITELIST = utils.getBoolean("general.worldedit.whitelist", false);
        GENERAL_WORLDEDIT_LIST = utils.getSet("general.worldedit.list", Utils.SCANNABLE_BLOCKS, "block", new CaseInsensitiveHashSet());

        GENERAL_NOTIFICATION_TYPE = utils.getString("general.notification.type", "BOSSBAR", GENERAL_NOTIFICATION_TYPE_VALUES);
        GENERAL_NOTIFICATION_BOSSBAR_COLOR = utils.getString("general.notification.bossbar.color", "BLUE", GENERAL_NOTIFICATION_BOSSBAR_COLOR_VALUES);
        GENERAL_NOTIFICATION_BOSSBAR_STYLE = utils.getString("general.notification.bossbar.style", "SEGMENTED_10", GENERAL_NOTIFICATION_BOSSBAR_STYLE_VALUES);
        GENERAL_NOTIFICATION_BOSSBAR_FLAGS = utils.getStringList("general.notification.bossbar.flags", GENERAL_NOTIFICATION_BOSSBAR_FLAGS_VALUES, "flag");
        GENERAL_NOTIFICATION_BOSSBAR_DURATION = utils.getIntWithinRange("general.notification.bossbar.duration", 60, 1, null);

        GENERAL_SCANRADIUS_DEFAULT = utils.getIntWithinRange("general.scanradius_default", 5, 1, null);
        GENERAL_SCAN_NOTIFICATION = utils.getBoolean("general.scan_notification", true);
        GENERAL_ALWAYS_SHOW_NOTIFICATION = utils.getBoolean("general.always_show_notification", true);

        GENERAL_NOTIFICATION_PASSIVE = utils.getStringList("general.passive", GENERAL_NOTIFICATION_PASSIVE_VALUES, "passive value");

        this.limits.reload(utils);

        GENERAL_REGIONS_WHITELIST = utils.getBoolean("general.regions.whitelist", false);
        GENERAL_REGIONS_LIST = utils.getStringList("general.regions.list");
    }

    public static Set<String> of(String... strings) {
        return new HashSet<>(Arrays.asList(strings));
    }

    public Limits getLimits() {
        return limits;
    }
}
