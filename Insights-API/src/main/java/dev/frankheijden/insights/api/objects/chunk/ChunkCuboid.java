package dev.frankheijden.insights.api.objects.chunk;

import org.bukkit.World;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record ChunkCuboid(ChunkVector min, ChunkVector max) {

    private static final Map<UUID, ChunkCuboid> maxCuboidCache = new HashMap<>();

    public long getVolume() {
        return (max.x() - min.x() + 1L) * (max.y() - min.y() + 1L) * (max.x() - min.z() + 1L);
    }

    /**
     * Determines whether the specified cuboid "fits" in this cuboid instance.
     */
    public boolean contains(ChunkCuboid other) {
        return this.min.x() <= other.min.x()
                && this.min.y() <= other.min.y()
                && this.min.z() <= other.min.z()
                && this.max.x() >= other.max.x()
                && this.max.y() >= other.max.y()
                && this.max.z() >= other.max.z();
    }

    /**
     * Determines the maximum ChunkCuboid of a given world, and caches the result.
     */
    public static ChunkCuboid maxCuboid(World world) {
        return maxCuboidCache.computeIfAbsent(world.getUID(), k -> new ChunkCuboid(
                ChunkVector.minVector(world),
                ChunkVector.maxVector(world)
        ));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkCuboid that = (ChunkCuboid) o;
        return min.equals(that.min) && max.equals(that.max);
    }

    @Override
    public String toString() {
        return min + " -> " + max;
    }
}
