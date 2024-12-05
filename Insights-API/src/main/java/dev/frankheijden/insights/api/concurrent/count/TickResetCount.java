package dev.frankheijden.insights.api.concurrent.count;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.utils.SchedulingUtils;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TickResetCount<T> {

    private final InsightsPlugin plugin;
    private final int intervalTicks;
    private final int size;
    private final Map<T, IntegerCount> counts;
    private final ResetTask resetTask;
    private ScheduledTask scheduledTask = null;

    /**
     * Constructs a new TickResetCount.
     */
    public TickResetCount(InsightsPlugin plugin, int intervalTicks, int size) {
        this.plugin = plugin;
        this.intervalTicks = intervalTicks;
        this.size = size;
        this.counts = new ConcurrentHashMap<>();
        this.resetTask = new ResetTask();
    }

    /**
     * Starts the reset task.
     * @throws IllegalStateException If the task is already running.
     */
    public void start() {
        if (scheduledTask != null) {
            throw new IllegalStateException("ResetTask is already running!");
        }

        this.scheduledTask = SchedulingUtils.runTaskTimerAsynchronously(
                plugin,
                resetTask,
                1L,
                intervalTicks
        );
    }

    /**
     * Stops the reset task.
     * @throws IllegalStateException If the task is not running.
     */
    public void stop() {
        if (scheduledTask == null) {
            throw new IllegalStateException("ResetTask is not running!");
        }

        this.scheduledTask.cancel();
        this.scheduledTask = null;
    }

    public int getCurrentTick() {
        return resetTask.tick;
    }

    public int increment(T key) {
        return counts.computeIfAbsent(key, k -> new IntegerCount(size)).increment(resetTask.tick);
    }

    public void remove(T key) {
        counts.remove(key);
    }

    public class ResetTask implements Runnable {

        private int tick;

        public ResetTask() {
            this.tick = 0;
        }

        @Override
        public void run() {
            int nextTick = tick + 1;
            if (nextTick >= size) {
                nextTick = 0;
            }

            for (IntegerCount value : counts.values()) {
                value.reset(nextTick);
            }

            this.tick = nextTick;
        }
    }
}
