package dev.frankheijden.insights.api.concurrent.storage;

import org.bukkit.Material;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
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

    public Optional<Integer> getDistribution(UUID worldUid, long chunkKey, Collection<? extends Material> materials) {
        return getChunkDistribution(worldUid).count(chunkKey, materials);
    }

    public void put(UUID worldUid, long chunkKey, Map<Material, Integer> map) {
        getChunkDistribution(worldUid).put(chunkKey, map);
    }

    public void modify(UUID worldUid, long chunkKey, Material m, int amount) {
        getChunkDistribution(worldUid).modify(chunkKey, m, amount);
    }

    public void remove(UUID worldUid, long chunkKey) {
        getChunkDistribution(worldUid).remove(chunkKey);
    }

    public boolean contains(UUID worldUid, long chunkKey) {
        return getChunkDistribution(worldUid).contains(chunkKey);
    }
}
