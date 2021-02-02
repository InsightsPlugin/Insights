package dev.frankheijden.insights.api.objects;

import dev.frankheijden.insights.api.InsightsPlugin;

public abstract class InsightsBase {

    protected final InsightsPlugin plugin;

    protected InsightsBase(InsightsPlugin plugin) {
        this.plugin = plugin;
    }
}
