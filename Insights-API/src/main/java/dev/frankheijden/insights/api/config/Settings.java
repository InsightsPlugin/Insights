package dev.frankheijden.insights.api.config;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.config.parser.PassiveYamlParser;
import dev.frankheijden.insights.api.config.parser.YamlParser;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Settings {

    public final int CONCURRENT_SCAN_THREADS;
    public final ChunkScanMode CHUNK_SCAN_MODE;
    public final NotificationType NOTIFICATION_TYPE;
    public final BarColor NOTIFICATION_BOSSBAR_COLOR;
    public final BarStyle NOTIFICATION_BOSSBAR_STYLE;
    public final BarFlag[] NOTIFICATION_BOSSBAR_FLAGS;
    public final int NOTIFICATION_BOSSBAR_DURATION_TICKS;
    public final int NOTIFICATION_ACTIONBAR_SEGMENTS;
    public final String NOTIFICATION_ACTIONBAR_SEQUENCE;
    public final String NOTIFICATION_ACTIONBAR_DONE_COLOR;
    public final String NOTIFICATION_ACTIONBAR_TOTAL_COLOR;
    public final String NOTIFICATION_ACTIONBAR_SEPARATOR;
    public final List<Class<?>> DISABLED_EVENTS;
    public final boolean WORLDEDIT_INTEGRATION_ENABLED;
    public final WorldEditIntegrationType WORLDEDIT_INTEGRATION_TYPE;
    public final Material WORLDEDIT_INTEGRATION_REPLACEMENT_BLOCK;

    /**
     * Constructs a new Settings object from the given YamlParser.
     */
    @SuppressWarnings("LineLength")
    public Settings(InsightsPlugin plugin, YamlParser parser) {
        int maxThreads = Runtime.getRuntime().availableProcessors();
        int threads = parser.getInt("settings.concurrent-scan-threads", -1, -1, maxThreads);
        if (threads <= 0) threads = maxThreads;
        CONCURRENT_SCAN_THREADS = threads;
        CHUNK_SCAN_MODE = parser.getEnum("settings.chunk-scan-mode", ChunkScanMode.MODIFICATION);

        NOTIFICATION_TYPE = parser.getEnum("settings.notification.type", NotificationType.BOSSBAR);

        NOTIFICATION_BOSSBAR_COLOR = parser.getEnum("settings.notification.bossbar.color", BarColor.BLUE);
        NOTIFICATION_BOSSBAR_STYLE = parser.getEnum("settings.notification.bossbar.style", BarStyle.SEGMENTED_10);
        NOTIFICATION_BOSSBAR_FLAGS = parser.getEnums("settings.notification.bossbar.flags", BarFlag.class).toArray(new BarFlag[0]);
        NOTIFICATION_BOSSBAR_DURATION_TICKS = parser.getInt("settings.notification.bossbar.duration-ticks", 60, 0, Integer.MAX_VALUE);

        NOTIFICATION_ACTIONBAR_SEGMENTS = parser.getInt("settings.notification.actionbar.segments", 50, 0, 100);
        NOTIFICATION_ACTIONBAR_SEQUENCE = parser.getString("settings.notification.actionbar.progress-sequence", "|");
        NOTIFICATION_ACTIONBAR_DONE_COLOR = parser.getString("settings.notification.actionbar.done-color", "&a");
        NOTIFICATION_ACTIONBAR_TOTAL_COLOR = parser.getString("settings.notification.actionbar.total-color", "&8");
        NOTIFICATION_ACTIONBAR_SEPARATOR = parser.getString("settings.notification.actionbar.separator", " ");

        DISABLED_EVENTS = new ArrayList<>();
        Map<String, Class<?>> events = plugin.getAllowedDisableEvents();
        for (String str : parser.getSet("settings.disabled-listeners", events.keySet(), "event")) {
            DISABLED_EVENTS.add(events.get(str));
        }

        WORLDEDIT_INTEGRATION_ENABLED = parser.getBoolean("settings.worldedit-integration.enabled", true);
        WORLDEDIT_INTEGRATION_TYPE = parser.getEnum("settings.worldedit-integration.type", WorldEditIntegrationType.REPLACEMENT);
        WORLDEDIT_INTEGRATION_REPLACEMENT_BLOCK = parser.getEnum("settings.worldedit-integration.replacement-block", Material.BEDROCK);
    }

    /**
     * Loads the given File, with given default settings as InputStream.
     * @return A Monad wrap of the Settings object.
     */
    public static Monad<Settings> load(InsightsPlugin plugin, File file, InputStream defaultSettings) throws IOException {
        PassiveYamlParser parser = PassiveYamlParser.load(file, defaultSettings);
        Settings settings = new Settings(plugin, parser);
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
