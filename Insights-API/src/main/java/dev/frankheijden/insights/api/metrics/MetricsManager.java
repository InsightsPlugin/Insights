package dev.frankheijden.insights.api.metrics;

import dev.frankheijden.insights.api.InsightsPlugin;
import java.util.concurrent.atomic.LongAdder;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SingleLineChart;

public class MetricsManager {

    private static final int BSTATS_METRICS_ID = 7272;

    private final IntegerMetric chunkScanMetric = new IntegerMetric();
    private final IntegerMetric limitMetric = new IntegerMetric();
    private final LongAdder totalBlocksScanned = new LongAdder();

    /**
     * Constructs a new MetricsManager with some extra charts.
     */
    public MetricsManager(InsightsPlugin plugin) {
        var metrics = new Metrics(plugin, BSTATS_METRICS_ID);
        metrics.addCustomChart(new SingleLineChart("chunks-scanned", chunkScanMetric));
        metrics.addCustomChart(new SingleLineChart("limits", limitMetric));
    }

    public IntegerMetric getChunkScanMetric() {
        return chunkScanMetric;
    }

    public IntegerMetric getLimitMetric() {
        return limitMetric;
    }

    public LongAdder getTotalBlocksScanned() {
        return totalBlocksScanned;
    }
}
