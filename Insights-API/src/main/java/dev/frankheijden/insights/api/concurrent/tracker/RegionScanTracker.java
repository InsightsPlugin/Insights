package dev.frankheijden.insights.api.concurrent.tracker;

import dev.frankheijden.insights.api.region.Region;
import org.checkerframework.checker.nullness.qual.NonNull;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RegionScanTracker {

    private final Map<UUID, Set<UUID>> trackedWorlds;

    public RegionScanTracker() {
        this.trackedWorlds = new ConcurrentHashMap<>();
    }

    private @NonNull Set<UUID> getWorldTracker(@NonNull UUID worldUuid) {
        return trackedWorlds.computeIfAbsent(worldUuid, uuid -> ConcurrentHashMap.newKeySet());
    }

    public boolean setQueued(@NonNull Region region, boolean queued) {
        Set<UUID> worldTracker = getWorldTracker(region.worldUuid());
        return queued ? worldTracker.add(region.regionUuid()) : worldTracker.remove(region.regionUuid());
    }

    public boolean isQueued(@NonNull Region region) {
        return getWorldTracker(region.worldUuid()).contains(region.regionUuid());
    }
}
