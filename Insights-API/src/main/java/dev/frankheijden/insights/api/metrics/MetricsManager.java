package dev.frankheijden.insights.api.metrics;

import dev.frankheijden.insights.api.InsightsPlugin;
import org.bstats.bukkit.Metrics;

public class MetricsManager {

    private static final int BSTATS_METRICS_ID = 7272;

    private final Metrics metrics;

    public MetricsManager(InsightsPlugin plugin) {
        metrics = new Metrics(plugin, BSTATS_METRICS_ID);
    }
}
