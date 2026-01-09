package dev.frankheijden.insights.api.concurrent.storage;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Thread-safe LRU cache with TTL for addon region storage.
 */
public class AddonStorage {

    private static final int DEFAULT_MAX_SIZE = 500;
    private static final long DEFAULT_TTL_MINUTES = 10;
    private static final long CLEANUP_INTERVAL_MINUTES = 1;

    private final Map<String, CacheEntry> distributionMap;
    private final int maxSize;
    private final long ttlMillis;
    private final ScheduledExecutorService cleanupExecutor;

    public AddonStorage() {
        this(DEFAULT_MAX_SIZE, DEFAULT_TTL_MINUTES);
    }

    public AddonStorage(int maxSize, long ttlMinutes) {
        this.distributionMap = new ConcurrentHashMap<>();
        this.maxSize = maxSize;
        this.ttlMillis = TimeUnit.MINUTES.toMillis(ttlMinutes);
        this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Insights-AddonStorage-Cleanup");
            t.setDaemon(true);
            return t;
        });
        cleanupExecutor.scheduleAtFixedRate(
            this::cleanupExpiredEntries,
            CLEANUP_INTERVAL_MINUTES,
            CLEANUP_INTERVAL_MINUTES,
            TimeUnit.MINUTES
        );
    }

    public Optional<Storage> get(String key) {
        CacheEntry entry = distributionMap.get(key);
        if (entry == null) {
            return Optional.empty();
        }
        if (entry.isExpired(ttlMillis)) {
            distributionMap.remove(key, entry);
            return Optional.empty();
        }
        entry.touch();
        return Optional.of(entry.getStorage());
    }

    public void put(String key, Storage storage) {
        while (distributionMap.size() >= maxSize) {
            evictOldest();
        }
        distributionMap.put(key, new CacheEntry(storage));
    }

    public void remove(String key) {
        distributionMap.remove(key);
    }

    public int size() {
        return distributionMap.size();
    }

    public void clear() {
        distributionMap.clear();
    }

    public void shutdown() {
        cleanupExecutor.shutdown();
        try {
            if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanupExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void cleanupExpiredEntries() {
        long now = System.currentTimeMillis();
        distributionMap.entrySet().removeIf(entry -> entry.getValue().isExpired(ttlMillis, now));
    }

    private void evictOldest() {
        String oldestKey = null;
        long oldestTime = Long.MAX_VALUE;
        for (Map.Entry<String, CacheEntry> entry : distributionMap.entrySet()) {
            long accessTime = entry.getValue().getLastAccessTime();
            if (accessTime < oldestTime) {
                oldestTime = accessTime;
                oldestKey = entry.getKey();
            }
        }
        if (oldestKey != null) {
            distributionMap.remove(oldestKey);
        }
    }

    private static class CacheEntry {
        private final Storage storage;
        private final long creationTime;
        private volatile long lastAccessTime;

        CacheEntry(Storage storage) {
            this.storage = storage;
            this.creationTime = System.currentTimeMillis();
            this.lastAccessTime = this.creationTime;
        }

        Storage getStorage() {
            return storage;
        }

        long getLastAccessTime() {
            return lastAccessTime;
        }

        void touch() {
            this.lastAccessTime = System.currentTimeMillis();
        }

        boolean isExpired(long ttlMillis) {
            return isExpired(ttlMillis, System.currentTimeMillis());
        }

        boolean isExpired(long ttlMillis, long currentTime) {
            return (currentTime - creationTime) > ttlMillis;
        }
    }
}
