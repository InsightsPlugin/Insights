package dev.frankheijden.insights.api.tasks;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.objects.InsightsBase;

public abstract class InsightsTask extends InsightsBase implements Runnable {

    protected InsightsTask(InsightsPlugin plugin) {
        super(plugin);
    }
}
