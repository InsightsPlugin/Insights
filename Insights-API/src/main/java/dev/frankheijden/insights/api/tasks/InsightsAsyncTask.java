package dev.frankheijden.insights.api.tasks;

import dev.frankheijden.insights.api.InsightsPlugin;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class InsightsAsyncTask extends InsightsTask {

    protected final AtomicBoolean run = new AtomicBoolean(true);

    protected InsightsAsyncTask(InsightsPlugin plugin) {
        super(plugin);
    }

    @Override
    public void run() {
        if (!run.getAndSet(false)) return;
        runInternal();
    }

    protected abstract void runInternal();
}
