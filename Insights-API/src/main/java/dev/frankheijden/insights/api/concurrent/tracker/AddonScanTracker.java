package dev.frankheijden.insights.api.concurrent.tracker;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe tracker for addon region scans in progress.
 */
public class AddonScanTracker {

    private final Set<String> tracker;

    public AddonScanTracker() {
        this.tracker = Collections.newSetFromMap(new ConcurrentHashMap<>());
    }

    public void add(String key) {
        this.tracker.add(key);
    }

    /**
     * Atomically adds key if not present. Returns true if added, false if already exists.
     */
    public boolean tryAdd(String key) {
        return this.tracker.add(key);
    }

    public boolean isQueued(String key) {
        return this.tracker.contains(key);
    }

    public void remove(String key) {
        this.tracker.remove(key);
    }

    public int size() {
        return this.tracker.size();
    }

    public void clear() {
        this.tracker.clear();
    }
}
