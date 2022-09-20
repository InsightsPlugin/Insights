package dev.frankheijden.insights.api.config;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.config.parser.PassiveYamlParser;
import dev.frankheijden.insights.api.config.parser.YamlParser;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class Settings {

    public final boolean UPDATE_CHECKER_ENABLED;
    public final int UPDATE_CHECKER_INTERVAL_SECONDS;
    public final int SCANS_CONCURRENT_THREADS;
    public final int SCANS_TIMEOUT_MILLIS;
    public final int SCANS_ITERATION_INTERVAL_TICKS;
    public final int SCANS_CHUNKS_PER_ITERATION;
    public final int SCANS_INFO_INTERVAL_MILLIS;
    public final ChunkScanMode CHUNK_SCANS_MODE;
    public final int CHUNK_SCANS_PLAYER_TRACKER_INTERVAL_TICKS;
    public final NotificationType NOTIFICATION_TYPE;
    public final BossBar.Color NOTIFICATION_BOSSBAR_COLOR;
    public final BossBar.Overlay NOTIFICATION_BOSSBAR_OVERLAY;
    public final Set<BossBar.Flag> NOTIFICATION_BOSSBAR_FLAGS;
    public final int NOTIFICATION_BOSSBAR_DURATION_TICKS;
    public final int NOTIFICATION_ACTIONBAR_SEGMENTS;
    public final String NOTIFICATION_ACTIONBAR_SEQUENCE;
    public final String NOTIFICATION_ACTIONBAR_DONE_COLOR;
    public final String NOTIFICATION_ACTIONBAR_TOTAL_COLOR;
    public final String NOTIFICATION_ACTIONBAR_SEPARATOR;
    public final boolean AREA_SCAN_NOTIFICATIONS_ENABLED;
    public final String AREA_SCAN_NOTIFICATIONS_PERMISSION;
    public final int SPIGOT_ENTITY_TRACKER_INTERVAL_TICKS;
    public final boolean APPLY_PISTON_LIMITS;
    public final int PAGINATION_RESULTS_PER_PAGE;
    public final List<Class<? extends Event>> DISABLED_EVENTS;
    public final Map<Class<? extends Event>, EventPriority> LISTENER_PRIORITIES;
    public final boolean REDSTONE_UPDATE_LIMITER_ENABLED;
    public final int REDSTONE_UPDATE_LIMITER_LIMIT;
    public final int REDSTONE_UPDATE_AGGREGATE_TICKS;
    public final int REDSTONE_UPDATE_AGGREGATE_SIZE;
    public final boolean REDSTONE_UPDATE_LIMITER_BLOCK_OUTSIDE_REGION;

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
        SCANS_TIMEOUT_MILLIS = parser.getInt("settings.scans.timeout-millis", 10000, 1, Integer.MAX_VALUE);
        SCANS_ITERATION_INTERVAL_TICKS = parser.getInt("settings.scans.iteration-interval-ticks", 1, 1, Integer.MAX_VALUE);
        SCANS_CHUNKS_PER_ITERATION = parser.getInt("settings.scans.chunks-per-iteration", 2, 1, Integer.MAX_VALUE);
        SCANS_INFO_INTERVAL_MILLIS = parser.getInt("settings.scans.info-interval-millis", 50, 1, Integer.MAX_VALUE);

        CHUNK_SCANS_MODE = parser.getEnum("settings.chunk-scans.mode", ChunkScanMode.ALWAYS);
        CHUNK_SCANS_PLAYER_TRACKER_INTERVAL_TICKS = parser.getInt("settings.chunk-scans.player-tracker-interval-ticks", 5, 1, Integer.MAX_VALUE);

        NOTIFICATION_TYPE = parser.getEnum("settings.notification.type", NotificationType.BOSSBAR);

        NOTIFICATION_BOSSBAR_COLOR = parser.getEnum("settings.notification.bossbar.color", BossBar.Color.BLUE);
        NOTIFICATION_BOSSBAR_OVERLAY = parser.getEnum("settings.notification.bossbar.overlay", BossBar.Overlay.NOTCHED_10);
        var flags = parser.getEnums("settings.notification.bossbar.flags", BossBar.Flag.class);
        NOTIFICATION_BOSSBAR_FLAGS = flags.isEmpty() ? Collections.emptySet() : EnumSet.copyOf(flags);
        NOTIFICATION_BOSSBAR_DURATION_TICKS = parser.getInt("settings.notification.bossbar.duration-ticks", 60, 0, Integer.MAX_VALUE);

        NOTIFICATION_ACTIONBAR_SEGMENTS = parser.getInt("settings.notification.actionbar.segments", 50, 0, 100);
        NOTIFICATION_ACTIONBAR_SEQUENCE = parser.getString("settings.notification.actionbar.progress-sequence", "|");
        NOTIFICATION_ACTIONBAR_DONE_COLOR = parser.getString("settings.notification.actionbar.done-color", "&a");
        NOTIFICATION_ACTIONBAR_TOTAL_COLOR = parser.getString("settings.notification.actionbar.total-color", "&8");
        NOTIFICATION_ACTIONBAR_SEPARATOR = parser.getString("settings.notification.actionbar.separator", " ");

        AREA_SCAN_NOTIFICATIONS_ENABLED = parser.getBoolean("settings.area-scan-notifications.enabled", true);
        AREA_SCAN_NOTIFICATIONS_PERMISSION = parser.getString("settings.area-scan-notifications.permission", "");

        SPIGOT_ENTITY_TRACKER_INTERVAL_TICKS = parser.getInt("settings.spigot.entity-tracker-interval-ticks", 10, 1, Integer.MAX_VALUE);
        APPLY_PISTON_LIMITS = parser.getBoolean("settings.apply-piston-limits", true);
        PAGINATION_RESULTS_PER_PAGE = parser.getInt("settings.pagination-results-per-page", 6, 1, Integer.MAX_VALUE);

        DISABLED_EVENTS = new ArrayList<>();
        Map<String, Method> disableEvents = plugin.getListenerManager().getAllowedDisableMethods();
        for (String str : parser.getSet("settings.disabled-listeners", disableEvents.keySet(), "event")) {
            @SuppressWarnings("unchecked")
            var eventClass = (Class<? extends Event>) disableEvents.get(str).getParameterTypes()[0];
            DISABLED_EVENTS.add(eventClass);
        }

        LISTENER_PRIORITIES = new HashMap<>();
        Map<String, Method> overrideEvents = plugin.getListenerManager().getAllowedPriorityOverrideMethods();
        for (String event : parser.getKeys("settings.listener-priorities")) {
            String eventUppercase = event.toUpperCase(Locale.ENGLISH);

            @SuppressWarnings("unchecked")
            var eventClass = (Class<? extends Event>) overrideEvents.get(eventUppercase).getParameterTypes()[0];
            if (eventClass == null) continue;

            LISTENER_PRIORITIES.put(
                    eventClass,
                    parser.getEnum("settings.listener-priorities." + event, EventPriority.LOWEST)
            );
        }

        REDSTONE_UPDATE_LIMITER_ENABLED = parser.getBoolean("settings.redstone-update-limiter.enabled", false);
        REDSTONE_UPDATE_LIMITER_LIMIT = parser.getInt("settings.redstone-update-limiter.limit", 50000, 0, Integer.MAX_VALUE);
        REDSTONE_UPDATE_AGGREGATE_TICKS = parser.getInt("settings.redstone-update-limiter.aggregate-ticks", 10, 1, 20 * 60 * 60);
        REDSTONE_UPDATE_AGGREGATE_SIZE = parser.getInt("settings.redstone-update-limiter.aggregate-size", 30, 1, 20 * 60 * 60) + 1;
        REDSTONE_UPDATE_LIMITER_BLOCK_OUTSIDE_REGION = parser.getBoolean("settings.redstone-update-limiter.block-outside-region", false);
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

    public boolean canReceiveAreaScanNotifications(Player player) {
        return AREA_SCAN_NOTIFICATIONS_ENABLED && player.hasPermission(AREA_SCAN_NOTIFICATIONS_PERMISSION);
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
