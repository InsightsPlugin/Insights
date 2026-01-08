package dev.frankheijden.insights.api.concurrent.storage;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ChunkStorage {

    private static final int MAX_CACHED_CHUNKS = 5000;
    private final Map<Long, Storage> distributionMap;

    public ChunkStorage() {
        // LRU cache with 5000 chunk limit to avoid re-scanning on chunk reload
        this.distributionMap = Collections.synchronizedMap(
            new LinkedHashMap<>(MAX_CACHED_CHUNKS, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<Long, Storage> eldest) {
                    return size() > MAX_CACHED_CHUNKS;
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
}
