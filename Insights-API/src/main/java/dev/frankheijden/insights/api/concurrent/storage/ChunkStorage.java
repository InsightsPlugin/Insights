package dev.frankheijden.insights.api.concurrent.storage;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkStorage {

    private final Map<Long, DistributionStorage> distributionMap;

    public ChunkStorage() {
        this.distributionMap = new ConcurrentHashMap<>();
    }

    public Set<Long> getChunks() {
        return distributionMap.keySet();
    }

    public Optional<DistributionStorage> get(long chunkKey) {
        return Optional.ofNullable(distributionMap.get(chunkKey));
    }

    public void put(long chunkKey, DistributionStorage chunkDistribution) {
        distributionMap.put(chunkKey, chunkDistribution);
    }

    public void remove(long chunkKey) {
        distributionMap.remove(chunkKey);
    }
}
