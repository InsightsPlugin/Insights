package net.frankheijden.insights.objects;

import org.bukkit.Chunk;

public class ChunkLocation {
    private int x;
    private int z;

    /**
     *
     * @param x Chunk x coordinate
     * @param z Chunk z coordinate
     */
    public ChunkLocation(int x, int z) {
        this.x = x;
        this.z = z;
    }

    /**
     *
     * @param chunk Chunk
     */
    public ChunkLocation(Chunk chunk) {
        this(chunk.getX(), chunk.getZ());
    }

    /**
     *
     * @return Chunk x coordinate
     */
    public int getX() {
        return x;
    }

    /**
     *
     * @return Chunk z coordinate
     */
    public int getZ() {
        return z;
    }
}
