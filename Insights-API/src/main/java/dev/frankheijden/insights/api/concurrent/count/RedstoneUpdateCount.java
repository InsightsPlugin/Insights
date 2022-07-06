package dev.frankheijden.insights.api.concurrent.count;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.region.Region;
import dev.frankheijden.insights.api.util.Pair;
import org.checkerframework.checker.nullness.qual.NonNull;
import java.util.UUID;

public class RedstoneUpdateCount {

    private final TickResetCount<Pair<UUID, UUID>> counts;

    /**
     * Constructs a new RedstoneUpdateCount.
     */
    public RedstoneUpdateCount(InsightsPlugin plugin) {
        this.counts = new TickResetCount<>(
                plugin,
                plugin.settings().REDSTONE_UPDATE_AGGREGATE_TICKS,
                plugin.settings().REDSTONE_UPDATE_AGGREGATE_SIZE
        );
    }

    public void start() {
        this.counts.start();
    }

    public void stop() {
        this.counts.stop();
    }

    private Pair<UUID, UUID> key(Region region) {
        return new Pair<>(region.worldUuid(), region.regionUuid());
    }

    public int increment(@NonNull Region region) {
        return counts.increment(key(region));
    }

    public void remove(@NonNull Region region) {
        counts.remove(key(region));
    }
}
