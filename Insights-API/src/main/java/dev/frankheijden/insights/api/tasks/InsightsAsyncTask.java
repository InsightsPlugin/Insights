package dev.frankheijden.insights.api.tasks;

import dev.frankheijden.insights.api.InsightsPlugin;

public abstract class InsightsAsyncTask extends InsightsTask {

    protected InsightsAsyncTask(InsightsPlugin plugin) {
        super(plugin);
    }
}
