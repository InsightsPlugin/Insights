package dev.frankheijden.insights.api.concurrent.storage;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Thread-safe LRU cache for chunk storage.
 *
 * <p>Backed by fixed-size shards, each guarded by its own lock, so that concurrent
 * access to different chunks (e.g. Folia ticking many regions of the same world in
 * parallel) doesn't serialize on a single map-wide lock. Eviction is approximate:
 * each shard enforces its own share of {@link #maxSize}, rather than one global order.
 */
public class ChunkStorage {

    private static final int DEFAULT_MAX_CACHED_CHUNKS = 5000;
    private static final int SHARD_COUNT = 16;

    private final int maxSize;
    private final Shard[] shards;

    public ChunkStorage() {
        this(DEFAULT_MAX_CACHED_CHUNKS);
    }

    public ChunkStorage(int maxSize) {
        this.maxSize = maxSize;
        int perShard = Math.max(1, maxSize / SHARD_COUNT);
        this.shards = new Shard[SHARD_COUNT];
        for (int i = 0; i < SHARD_COUNT; i++) {
            shards[i] = new Shard(perShard);
        }
    }

    private Shard shardFor(long chunkKey) {
        int h = Long.hashCode(chunkKey);
        h ^= (h >>> 16);
        return shards[h & (SHARD_COUNT - 1)];
    }

    public Set<Long> getChunks() {
        Set<Long> keys = new HashSet<>();
        for (Shard shard : shards) {
            keys.addAll(shard.keys());
        }
        return keys;
    }

    public Optional<Storage> get(long chunkKey) {
        return shardFor(chunkKey).get(chunkKey);
    }

    public void put(long chunkKey, Storage storage) {
        shardFor(chunkKey).put(chunkKey, storage);
    }

    public void remove(long chunkKey) {
        shardFor(chunkKey).remove(chunkKey);
    }

    public int size() {
        int total = 0;
        for (Shard shard : shards) {
            total += shard.size();
        }
        return total;
    }

    public int getMaxSize() {
        return maxSize;
    }

    private static final class Shard {

        private final int maxSize;
        private final Map<Long, Storage> map;

        Shard(int maxSize) {
            this.maxSize = maxSize;
            this.map = new LinkedHashMap<>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<Long, Storage> eldest) {
                    return size() > Shard.this.maxSize;
                }
            };
        }

        synchronized Optional<Storage> get(long key) {
            return Optional.ofNullable(map.get(key));
        }

        synchronized void put(long key, Storage storage) {
            map.put(key, storage);
        }

        synchronized void remove(long key) {
            map.remove(key);
        }

        synchronized int size() {
            return map.size();
        }

        synchronized Set<Long> keys() {
            return new HashSet<>(map.keySet());
        }
    }
}
