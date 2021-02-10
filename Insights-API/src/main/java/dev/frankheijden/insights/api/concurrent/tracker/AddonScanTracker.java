package dev.frankheijden.insights.api.concurrent.tracker;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AddonScanTracker {

    private final Set<String> tracker;

    public AddonScanTracker() {
        this.tracker = Collections.newSetFromMap(new ConcurrentHashMap<>());
    }

    public void add(String key) {
        this.tracker.add(key);
    }

    public boolean isQueued(String key) {
        return this.tracker.contains(key);
    }

    public void remove(String key) {
        this.tracker.remove(key);
    }
}
