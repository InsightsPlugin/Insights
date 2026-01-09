package dev.frankheijden.insights.api.concurrent.storage;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Thread-safe LRU cache for chunk storage.
 */
public class ChunkStorage {

    private static final int DEFAULT_MAX_CACHED_CHUNKS = 5000;
    private final Map<Long, Storage> distributionMap;
    private final int maxSize;

    public ChunkStorage() {
        this(DEFAULT_MAX_CACHED_CHUNKS);
    }

    public ChunkStorage(int maxSize) {
        this.maxSize = maxSize;
        this.distributionMap = Collections.synchronizedMap(
            new LinkedHashMap<>(maxSize, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<Long, Storage> eldest) {
                    return size() > maxSize;
                }
            }
        );
    }

    public Set<Long> getChunks() {
        return distributionMap.keySet();
    }

    public Optional<Storage> get(long chunkKey) {
        return Optional.ofNullable(distributionMap.get(chunkKey));
    }

    public void put(long chunkKey, Storage storage) {
        distributionMap.put(chunkKey, storage);
    }

    public void remove(long chunkKey) {
        distributionMap.remove(chunkKey);
    }

    public int size() {
        return distributionMap.size();
    }

    public int getMaxSize() {
        return maxSize;
    }
}
