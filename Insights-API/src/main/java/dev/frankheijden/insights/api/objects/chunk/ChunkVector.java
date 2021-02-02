package dev.frankheijden.insights.api.objects.chunk;

import org.bukkit.Location;

public class ChunkVector {

    public static final ChunkVector MIN = new ChunkVector(0, 0, 0);
    public static final ChunkVector MAX = new ChunkVector(16, 256, 16);

    private final int x;
    private final int y;
    private final int z;

    /**
     * Constructs a new ChunkVector with given x, y, z coordinates in the chunk.
     */
    public ChunkVector(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    /**
     * Constructs a new ChunkVector from the given location.
     */
    public static ChunkVector from(Location loc) {
        int x = loc.getBlockX() % 16;
        if (x < 0) x += 16;
        int z = loc.getBlockZ() % 16;
        if (z < 0) z += 16;
        return new ChunkVector(x, loc.getBlockY(), z);
    }

    /**
     * Creates a value-based minimum of the current vector with the one given.
     */
    public ChunkVector min(ChunkVector other) {
        return new ChunkVector(
                Math.min(x, other.x),
                Math.min(y, other.y),
                Math.min(z, other.z)
        );
    }

    /**
     * Creates a value-based maximum of the current vector with the one given.
     */
    public ChunkVector max(ChunkVector other) {
        return new ChunkVector(
                Math.max(x, other.x),
                Math.max(y, other.y),
                Math.max(z, other.z)
        );
    }
}
