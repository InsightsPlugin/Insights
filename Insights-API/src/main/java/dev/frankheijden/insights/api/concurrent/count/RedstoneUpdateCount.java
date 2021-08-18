package dev.frankheijden.insights.api.concurrent.count;

import dev.frankheijden.insights.api.InsightsPlugin;

public class RedstoneUpdateCount {

    private final TickResetCount<Long> chunkCounts;
    private final TickResetCount<String> addonCounts;

    /**
     * Constructs a new RedstoneUpdateCount.
     */
    public RedstoneUpdateCount(InsightsPlugin plugin) {
        this.chunkCounts = new TickResetCount<>(
                plugin,
                plugin.getSettings().REDSTONE_UPDATE_AGGREGATE_TICKS,
                plugin.getSettings().REDSTONE_UPDATE_AGGREGATE_SIZE
        );
        this.addonCounts = new TickResetCount<>(
                plugin,
                plugin.getSettings().REDSTONE_UPDATE_AGGREGATE_TICKS,
                plugin.getSettings().REDSTONE_UPDATE_AGGREGATE_SIZE
        );
    }

    public void start() {
        this.chunkCounts.start();
        this.addonCounts.start();
    }

    public void stop() {
        this.chunkCounts.stop();
        this.addonCounts.stop();
    }

    public int increment(long chunkKey) {
        return chunkCounts.increment(chunkKey);
    }

    public int increment(String regionKey) {
        return addonCounts.increment(regionKey);
    }

    public void remove(long chunkKey) {
        chunkCounts.remove(chunkKey);
    }

    public void remove(String regionKey) {
        addonCounts.remove(regionKey);
    }
}
