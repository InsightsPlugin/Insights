package dev.frankheijden.insights.api.config;

import dev.frankheijden.insights.api.config.parser.YamlParser;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class Settings {

    public final int CONCURRENT_SCAN_THREADS;
    public final ChunkScanMode CHUNK_SCAN_MODE;
    public final NotificationType PROGRESS_NOTIFICATION_TYPE;
    public final BarColor PROGRESS_BOSSBAR_COLOR;
    public final BarStyle PROGRESS_BOSSBAR_STYLE;
    public final BarFlag[] PROGRESS_BOSSBAR_FLAGS;
    public final int PROGRESS_BOSSBAR_DURATION_TICKS;
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

        PROGRESS_NOTIFICATION_TYPE = parser.getEnum("settings.progress-notification.type", NotificationType.BOSSBAR);
        PROGRESS_BOSSBAR_COLOR = parser.getEnum("settings.progress-notification.bossbar.color", BarColor.BLUE);
        PROGRESS_BOSSBAR_STYLE = parser.getEnum("settings.progress-notification.bossbar.style", BarStyle.SEGMENTED_10);
        PROGRESS_BOSSBAR_FLAGS = parser.getEnums("settings.progress-notification.bossbar.flags", BarFlag.class);
        PROGRESS_BOSSBAR_DURATION_TICKS = parser.getInt("settings.progress-notification.bossbar.duration-ticks", 60, 0, Integer.MAX_VALUE);

        WORLDEDIT_INTEGRATION_ENABLED = parser.getBoolean("settings.worldedit-integration.enabled", true);
        WORLDEDIT_INTEGRATION_TYPE = parser.getEnum("settings.worldedit-integration.type", WorldEditIntegrationType.REPLACEMENT);
        WORLDEDIT_INTEGRATION_REPLACEMENT_BLOCK = parser.getEnum("settings.worldedit-integration.replacement-block", Material.BEDROCK);
    }

    /**
     * Loads the given File, with given default settings as InputStream.
     * @return A Monad wrap of the Settings object.
     */
    public static Monad<Settings> load(File file, InputStream defaultSettings) throws IOException {
        YamlParser parser = YamlParser.load(file, defaultSettings);
        Settings settings = new Settings(parser);
        return parser.toMonad(settings);
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
