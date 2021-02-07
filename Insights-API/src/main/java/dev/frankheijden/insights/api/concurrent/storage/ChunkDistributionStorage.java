package dev.frankheijden.insights.api.concurrent.storage;

import dev.frankheijden.insights.api.utils.ChunkUtils;
import org.bukkit.Chunk;
import org.bukkit.Material;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkDistributionStorage extends DistributionStorage<Long, Material> {

    public ChunkDistributionStorage() {
        super(new ConcurrentHashMap<>());
    }

    public void put(Chunk chunk, Map<Material, Integer> map) {
        put(ChunkUtils.getKey(chunk), map);
    }
}
