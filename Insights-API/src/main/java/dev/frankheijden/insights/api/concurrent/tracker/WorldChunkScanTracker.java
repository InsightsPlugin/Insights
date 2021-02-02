package dev.frankheijden.insights.api.concurrent.tracker;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WorldChunkScanTracker {

    private final Map<UUID, ChunkScanTracker> scanTrackerMap;

    public WorldChunkScanTracker() {
        this.scanTrackerMap = new ConcurrentHashMap<>();
    }

    public ChunkScanTracker getChunkScanTracker(UUID worldUid) {
        return scanTrackerMap.computeIfAbsent(worldUid, k -> new ChunkScanTracker());
    }

    public void set(UUID worldUid, long chunkKey, boolean queued) {
        getChunkScanTracker(worldUid).set(chunkKey, queued);
    }

    public boolean isQueued(UUID worldUid, long chunkKey) {
        return getChunkScanTracker(worldUid).isQueued(chunkKey);
    }
}
