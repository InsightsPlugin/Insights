package dev.frankheijden.insights.api.region;

import dev.frankheijden.insights.api.objects.chunk.ChunkCuboid;
import dev.frankheijden.insights.api.objects.chunk.ChunkLocation;
import dev.frankheijden.insights.api.objects.chunk.ChunkPart;
import dev.frankheijden.insights.api.objects.chunk.ChunkVector;
import dev.frankheijden.insights.api.objects.math.Vector3;
import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.NonNull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class CuboidRegion extends Region {

    private final World world;
    private final Vector3 min;
    private final Vector3 max;

    /**
     * Constructs a new cuboid in given world, with given minimum and maximum vectors.
     */
    protected CuboidRegion(World world, Vector3 min, Vector3 max) {
        this.world = world;
        this.min = min;
        this.max = max;
    }

    public World world() {
        return world;
    }

    public Vector3 min() {
        return min;
    }

    public Vector3 max() {
        return max;
    }

    @Override
    public @NonNull UUID worldUuid() {
        return world().getUID();
    }

    /**
     * Converts this cuboid into a List of ChunkParts.
     */
    public @NonNull List<ChunkPart> generateChunkParts() {
        ChunkVector minV = ChunkVector.from(this.min);
        int minX = this.min.x() >> 4;
        int minZ = this.min.z() >> 4;

        ChunkVector maxV = ChunkVector.from(this.max);
        int maxX = this.max.x() >> 4;
        int maxZ = this.max.z() >> 4;

        List<ChunkPart> parts = new ArrayList<>(maxX - minX + 1 + maxZ - minZ + 1);
        for (int x = minX; x <= maxX; x++) {
            int xmin = (x == minX) ? minV.x() : 0;
            int ymin = minV.y();
            int xmax = (x == maxX) ? maxV.x() : 15;

            for (int z = minZ; z <= maxZ; z++) {
                int zmin = (z == minZ) ? minV.z() : 0;
                int ymax = maxV.y();
                int zmax = (z == maxZ) ? maxV.z() : 15;

                ChunkLocation loc = new ChunkLocation(world, x, z);
                ChunkVector vmin = new ChunkVector(xmin, ymin, zmin);
                ChunkVector vmax = new ChunkVector(xmax, ymax, zmax);
                parts.add(new ChunkPart(loc, new ChunkCuboid(vmin, vmax)));
            }
        }
        return parts;
    }
}
