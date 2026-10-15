package dev.frankheijden.insights.api.concurrent.tracker;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkScanTracker implements ScanTracker<Long> {

    private final Set<Long> queuedChunks;

    public ChunkScanTracker() {
        this.queuedChunks = Collections.newSetFromMap(new ConcurrentHashMap<>());
    }

    @Override
    public boolean set(Long obj, boolean queued) {
        return queued ? queuedChunks.add(obj) : queuedChunks.remove(obj);
    }

    @Override
    public boolean isQueued(Long obj) {
        return queuedChunks.contains(obj);
    }
}
