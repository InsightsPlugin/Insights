package net.frankheijden.insights.api.entities;

import org.bukkit.Chunk;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChunkLocation {
    private final int x;
    private final int z;

    /**
     * Constructs a new ChunkLocation from given x, y
     * @param x Chunk x coordinate
     * @param z Chunk z coordinate
     */
    public ChunkLocation(int x, int z) {
        this.x = x;
        this.z = z;
    }

    /**
     * Constructs a new ChunkLocation from a Chunk
     * @param chunk Chunk
     */
    public ChunkLocation(Chunk chunk) {
        this(chunk.getX(), chunk.getZ());
    }

    /**
     * Converts an array of chunks into a list of ChunkLocations
     * @param chunks Array of chunks
     * @return List with newly created ChunkLocations
     */
    public static List<ChunkLocation> from(Chunk... chunks) {
        return Stream.of(chunks)
                .map(ChunkLocation::new)
                .collect(Collectors.toList());
    }

    /**
     * Gets the X coordinate of the ChunkLocation
     * @return Chunk x coordinate
     */
    public int getX() {
        return x;
    }

    /**
     * Gets the Z coordinate of the ChunkLocation
     * @return Chunk z coordinate
     */
    public int getZ() {
        return z;
    }
}
