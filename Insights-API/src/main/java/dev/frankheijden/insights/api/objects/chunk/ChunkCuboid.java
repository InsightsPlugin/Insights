package dev.frankheijden.insights.api.objects.chunk;

import org.bukkit.World;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChunkCuboid {

    private static final Map<UUID, ChunkCuboid> maxCuboidCache = new HashMap<>();

    private final ChunkVector min;
    private final ChunkVector max;

    public ChunkCuboid(ChunkVector min, ChunkVector max) {
        this.min = min;
        this.max = max;
    }

    public ChunkVector getMin() {
        return min;
    }

    public ChunkVector getMax() {
        return max;
    }

    public long getVolume() {
        return (max.getX() - min.getX() + 1L) * (max.getY() - min.getY() + 1L) * (max.getX() - min.getZ() + 1L);
    }

    /**
     * Determines the maximum ChunkCuboid of a given world, and caches the result.
     */
    public static ChunkCuboid maxCuboid(World world) {
        return maxCuboidCache.computeIfAbsent(world.getUID(), k -> new ChunkCuboid(
                ChunkVector.minVector(),
                ChunkVector.maxVector(world)
        ));
    }
}
