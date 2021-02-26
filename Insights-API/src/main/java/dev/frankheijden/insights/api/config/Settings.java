package dev.frankheijden.insights.api.config;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.config.parser.PassiveYamlParser;
import dev.frankheijden.insights.api.config.parser.YamlParser;
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

    public final boolean UPDATE_CHECKER_ENABLED;
    public final int UPDATE_CHECKER_INTERVAL_SECONDS;
    public final int SCANS_CONCURRENT_THREADS;
    public final int SCANS_ITERATION_INTERVAL_TICKS;
    public final int SCANS_CHUNKS_PER_ITERATION;
    public final int SCANS_INFO_INTERVAL_MILLIS;
    public final ChunkScanMode CHUNK_SCANS_MODE;
    public final int CHUNK_SCANS_PLAYER_TRACKER_INTERVAL_TICKS;
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
    public final int SPIGOT_ENTITY_TRACKER_INTERVAL_TICKS;
    public final boolean APPLY_PISTON_LIMITS;
    public final List<Class<?>> DISABLED_EVENTS;

    /**
     * Constructs a new Settings object from the given YamlParser.
     */
    @SuppressWarnings("LineLength")
    public Settings(InsightsPlugin plugin, YamlParser parser) {
        UPDATE_CHECKER_ENABLED = parser.getBoolean("settings.update-checker.enabled", true);
        UPDATE_CHECKER_INTERVAL_SECONDS = parser.getInt("settings.update-checker.interval-seconds", 10800, 1, Integer.MAX_VALUE);

        int maxThreads = Runtime.getRuntime().availableProcessors();
        int threads = parser.getInt("settings.scans.concurrent-threads", -1, -1, maxThreads);
        if (threads <= 0) threads = maxThreads;
        SCANS_CONCURRENT_THREADS = threads;
        SCANS_ITERATION_INTERVAL_TICKS = parser.getInt("settings.scans.iteration-interval-ticks", 1, 1, Integer.MAX_VALUE);
        SCANS_CHUNKS_PER_ITERATION = parser.getInt("settings.scans.chunks-per-iteration", 2, 1, Integer.MAX_VALUE);
        SCANS_INFO_INTERVAL_MILLIS = parser.getInt("settings.scans.info-interval-millis", 50, 1, Integer.MAX_VALUE);

        CHUNK_SCANS_MODE = parser.getEnum("settings.chunk-scans.mode", ChunkScanMode.MODIFICATION);
        CHUNK_SCANS_PLAYER_TRACKER_INTERVAL_TICKS = parser.getInt("settings.chunk-scans.player-tracker-interval-ticks", 5, 1, Integer.MAX_VALUE);

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

        SPIGOT_ENTITY_TRACKER_INTERVAL_TICKS = parser.getInt("settings.spigot.entity-tracker-interval-ticks", 10, 1, Integer.MAX_VALUE);
        APPLY_PISTON_LIMITS = parser.getBoolean("settings.apply-piston-limits", true);

        DISABLED_EVENTS = new ArrayList<>();
        Map<String, Class<?>> events = plugin.getAllowedDisableEvents();
        for (String str : parser.getSet("settings.disabled-listeners", events.keySet(), "event")) {
            DISABLED_EVENTS.add(events.get(str));
        }
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
}
