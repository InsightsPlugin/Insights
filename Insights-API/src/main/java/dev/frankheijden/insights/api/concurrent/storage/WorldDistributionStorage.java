package dev.frankheijden.insights.api.concurrent.storage;

import org.bukkit.Material;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WorldDistributionStorage {

    private final Map<UUID, ChunkDistributionStorage> distributionMap;

    public WorldDistributionStorage() {
        this.distributionMap = new ConcurrentHashMap<>();
    }

    public ChunkDistributionStorage getChunkDistribution(UUID worldUid) {
        return distributionMap.computeIfAbsent(worldUid, k -> new ChunkDistributionStorage());
    }

    public void put(UUID worldUid, long chunkKey, Map<Material, Integer> map) {
        getChunkDistribution(worldUid).put(chunkKey, map);
    }

    public void remove(UUID worldUid, long chunkKey) {
        getChunkDistribution(worldUid).remove(chunkKey);
    }

    public boolean contains(UUID worldUid, long chunkKey) {
        return getChunkDistribution(worldUid).contains(chunkKey);
    }
}
