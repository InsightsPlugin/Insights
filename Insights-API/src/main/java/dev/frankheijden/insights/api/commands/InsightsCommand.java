package dev.frankheijden.insights.api.commands;

import dev.frankheijden.insights.api.InsightsPlugin;

public class InsightsCommand {

    protected final InsightsPlugin plugin;

    public InsightsCommand(InsightsPlugin plugin) {
        this.plugin = plugin;
    }
}
