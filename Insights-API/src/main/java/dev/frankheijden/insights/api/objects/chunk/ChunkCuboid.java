package dev.frankheijden.insights.api.objects.chunk;

import org.bukkit.World;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkCuboid {

    private static final Map<UUID, ChunkCuboid> maxCuboidCache = new ConcurrentHashMap<>();

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
        return (max.getX() - min.getX() + 1L) * (max.getY() - min.getY() + 1L) * (max.getZ() - min.getZ() + 1L);
    }

    /**
     * Determines whether the specified cuboid "fits" in this cuboid instance.
     */
    public boolean contains(ChunkCuboid other) {
        return this.min.getX() <= other.min.getX()
                && this.min.getY() <= other.min.getY()
                && this.min.getZ() <= other.min.getZ()
                && this.max.getX() >= other.max.getX()
                && this.max.getY() >= other.max.getY()
                && this.max.getZ() >= other.max.getZ();
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
    public int hashCode() {
        return Objects.hash(min, max);
    }

    @Override
    public String toString() {
        return min + " -> " + max;
    }
}
