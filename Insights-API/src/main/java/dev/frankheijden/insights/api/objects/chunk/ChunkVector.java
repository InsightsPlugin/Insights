package dev.frankheijden.insights.api.objects.chunk;

import dev.frankheijden.insights.api.objects.math.Vector3;
import org.bukkit.World;

public class ChunkVector extends Vector3 {

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
        return new ChunkVector(vector.getX() & 15, vector.getY(), vector.getZ() & 15);
    }

    public static ChunkVector minVector() {
        return new ChunkVector(0, 0, 0); // May change with 1.18
    }

    public static ChunkVector maxVector(World world) {
        return new ChunkVector(15, Math.abs(world.getMinHeight()) + world.getMaxHeight() - 1, 15);
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
