package dev.frankheijden.insights.api.objects.chunk;

import dev.frankheijden.insights.api.objects.math.Vector3;

public class ChunkVector extends Vector3 {

    public static final ChunkVector MIN = new ChunkVector(0, 0, 0);
    public static final ChunkVector MAX = new ChunkVector(16, 256, 16);

    /**
     * Constructs a new ChunkVector with given x, y, z coordinates in the chunk.
     */
    public ChunkVector(int x, int y, int z) {
        super(x, y, z);
    }

    /**
     * Constructs a new ChunkVector from the given vector.
     */
    public static ChunkVector from(Vector3 vector) {
        int x = vector.getX() % 16;
        if (x < 0) x += 16;
        int z = vector.getZ() % 16;
        if (z < 0) z += 16;
        return new ChunkVector(x, vector.getY(), z);
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
