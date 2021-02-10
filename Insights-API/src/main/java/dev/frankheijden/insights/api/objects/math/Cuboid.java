package dev.frankheijden.insights.api.objects.math;

import dev.frankheijden.insights.api.objects.chunk.ChunkCuboid;
import dev.frankheijden.insights.api.objects.chunk.ChunkLocation;
import dev.frankheijden.insights.api.objects.chunk.ChunkPart;
import dev.frankheijden.insights.api.objects.chunk.ChunkVector;
import org.bukkit.World;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Cuboid {

    private final World world;
    private final Vector3 min;
    private final Vector3 max;

    /**
     * Constructs a new cuboid in given world, with given minimum and maximum vectors.
     */
    public Cuboid(World world, Vector3 min, Vector3 max) {
        this.world = world;
        this.min = min;
        this.max = max;
    }

    public World getWorld() {
        return world;
    }

    public Vector3 getMin() {
        return min;
    }

    public Vector3 getMax() {
        return max;
    }

    /**
     * Converts this cuboid into a List of ChunkParts.
     */
    public List<ChunkPart> toChunkParts() {
        ChunkVector minV = ChunkVector.from(this.min);
        int minX = this.min.x >> 4;
        int minZ = this.min.z >> 4;

        ChunkVector maxV = ChunkVector.from(this.max);
        int maxX = this.max.x >> 4;
        int maxZ = this.max.z >> 4;

        List<ChunkPart> parts = new ArrayList<>(maxX - minX + 1 + maxZ - minZ + 1);
        for (int x = minX; x <= maxX; x++) {
            int xmin = (x == minX) ? minV.getX() : 0;
            int ymin = minV.getY();
            int xmax = (x == maxX) ? maxV.getX() : 15;

            for (int z = minZ; z <= maxZ; z++) {
                int zmin = (z == minZ) ? minV.getZ() : 0;
                int ymax = maxV.getY();
                int zmax = (z == maxZ) ? maxV.getZ() : 15;

                ChunkLocation loc = new ChunkLocation(world, x, z);
                ChunkVector vmin = new ChunkVector(xmin, ymin, zmin);
                ChunkVector vmax = new ChunkVector(xmax, ymax, zmax);
                parts.add(new ChunkPart(loc, new ChunkCuboid(vmin, vmax)));
            }
        }
        return parts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cuboid cuboid = (Cuboid) o;
        return world.equals(cuboid.world) && min.equals(cuboid.min) && max.equals(cuboid.max);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, min, max);
    }
}
