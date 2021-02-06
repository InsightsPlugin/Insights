package dev.frankheijden.insights.api.objects.chunk;

import org.bukkit.Chunk;
import org.bukkit.World;

public class ChunkLocation {

    private final World world;
    private final int x;
    private final int z;

    /**
     * Constructs a new ChunkLocation at given world, x and z coordinates of the chunk.
     */
    public ChunkLocation(World world, int x, int z) {
        this.world = world;
        this.x = x;
        this.z = z;
    }

    public static ChunkLocation of(Chunk chunk) {
        return new ChunkLocation(chunk.getWorld(), chunk.getX(), chunk.getZ());
    }

    public World getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public ChunkPart toPart() {
        return new ChunkPart(this);
    }
}
