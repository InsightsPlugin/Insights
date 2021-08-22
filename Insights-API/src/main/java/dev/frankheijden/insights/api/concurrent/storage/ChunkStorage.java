package dev.frankheijden.insights.api.concurrent.storage;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkStorage {

    private final Map<Long, Storage> distributionMap;

    public ChunkStorage() {
        this.distributionMap = new ConcurrentHashMap<>();
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
