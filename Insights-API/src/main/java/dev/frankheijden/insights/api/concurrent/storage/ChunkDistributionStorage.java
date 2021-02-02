package dev.frankheijden.insights.api.concurrent.storage;

import org.bukkit.Chunk;
import org.bukkit.Material;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkDistributionStorage extends DistributionStorage<Long, Material> {

    public ChunkDistributionStorage() {
        super(new ConcurrentHashMap<>());
    }

    public void put(Chunk chunk, Map<Material, Integer> map) {
        put(chunk.getChunkKey(), map);
    }

    public void remove(Chunk chunk) {
        remove(chunk.getChunkKey());
    }
}
