package dev.frankheijden.insights.api.config;

import dev.frankheijden.insights.api.config.parser.YamlParser;
import dev.frankheijden.insights.api.utils.YamlUtils;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Settings {

    public final int CONCURRENT_SCAN_THREADS;
    public final ChunkScanMode CHUNK_SCAN_MODE;
    public final NotificationType NOTIFICATION_TYPE;
    public final BarColor BOSSBAR_NOTIFICATION_COLOR;
    public final BarStyle BOSSBAR_NOTIFICATION_STYLE;
    public final BarFlag[] BOSSBAR_NOTIFICATION_FLAGS;
    public final int BOSSBAR_DURATION_TICKS;
    public final boolean WORLDEDIT_INTEGRATION_ENABLED;
    public final WorldEditIntegrationType WORLDEDIT_INTEGRATION_TYPE;
    public final Material WORLDEDIT_INTEGRATION_REPLACEMENT_BLOCK;

    /**
     * Constructs a new Settings object from the given YamlParser.
     */
    @SuppressWarnings("LineLength")
    public Settings(YamlParser parser) {
        int maxThreads = Runtime.getRuntime().availableProcessors();
        int threads = parser.getInt("settings.concurrent-scan-threads", -1, -1, maxThreads);
        if (threads <= 0) threads = maxThreads;
        CONCURRENT_SCAN_THREADS = threads;
        CHUNK_SCAN_MODE = parser.getEnum("settings.chunk-scan-mode", ChunkScanMode.MODIFICATION);

        NOTIFICATION_TYPE = parser.getEnum("settings.notification-type", NotificationType.BOSSBAR);

        BOSSBAR_NOTIFICATION_COLOR = parser.getEnum("settings.bossbar-notification.color", BarColor.BLUE);
        BOSSBAR_NOTIFICATION_STYLE = parser.getEnum("settings.bossbar-notification.style", BarStyle.SEGMENTED_10);
        BOSSBAR_NOTIFICATION_FLAGS = parser.getEnums("settings.bossbar-notification.flags", BarFlag.class);
        BOSSBAR_DURATION_TICKS = parser.getInt("settings.bossbar-notification.duration-ticks", 60, 0, Integer.MAX_VALUE);

        WORLDEDIT_INTEGRATION_ENABLED = parser.getBoolean("settings.worldedit-integration.enabled", true);
        WORLDEDIT_INTEGRATION_TYPE = parser.getEnum("settings.worldedit-integration.type", WorldEditIntegrationType.REPLACEMENT);
        WORLDEDIT_INTEGRATION_REPLACEMENT_BLOCK = parser.getEnum("settings.worldedit-integration.replacement-block", Material.BEDROCK);
    }

    /**
     * Loads the given File, with given default settings as InputStream.
     * @return A Monad wrap of the Settings object.
     */
    public static Monad<Settings> load(File file, InputStream defaultSettings) throws IOException {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        YamlConfiguration def = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultSettings));
        YamlUtils.update(yaml, def);
        YamlUtils.removeUnusedKeys(yaml, def);
        yaml.save(file);

        ConfigError.Builder errors = ConfigError.newBuilder();
        Settings settings = new Settings(new YamlParser(yaml, file.getName(), errors));
        return new Monad<>(settings, errors.getErrors());
    }

    public enum ChunkScanMode {
        ALWAYS,
        MODIFICATION
    }

    public enum NotificationType {
        BOSSBAR,
        ACTIONBAR
    }

    public enum WorldEditIntegrationType {
        REPLACEMENT,
        UNCHANGED
    }
}
