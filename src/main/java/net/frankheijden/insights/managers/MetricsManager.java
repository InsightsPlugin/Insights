package net.frankheijden.insights.managers;

import net.frankheijden.insights.Insights;
import org.bstats.bukkit.Metrics;

public class MetricsManager {

    private static final int BSTATS_METRICS_ID = 7272;

    private static MetricsManager instance;
    private final Metrics metrics;

    private static int SCAN_COUNT = 0;
    private static int LIMIT_COUNT = 0;

    public MetricsManager() {
        instance = this;

        metrics = new Metrics(Insights.getInstance(), BSTATS_METRICS_ID);
        metrics.addCustomChart(new Metrics.SingleLineChart("scans", this::resetScanCount));
        metrics.addCustomChart(new Metrics.SingleLineChart("limits", this::resetLimitCount));
    }

    public static MetricsManager getInstance() {
        return instance;
    }

    private Integer resetScanCount() {
        int count = SCAN_COUNT;
        SCAN_COUNT = 0;
        return count;
    }

    private Integer resetLimitCount() {
        int count = LIMIT_COUNT;
        LIMIT_COUNT = 0;
        return count;
    }

    public static void incrementScanCount() {
        if (SCAN_COUNT == Integer.MAX_VALUE) return;
        SCAN_COUNT++;
    }

    public static void incrementLimitCount() {
        if (LIMIT_COUNT == Integer.MAX_VALUE) return;
        LIMIT_COUNT++;
    }
}
