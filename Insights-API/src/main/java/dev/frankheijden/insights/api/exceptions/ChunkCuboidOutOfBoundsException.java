package dev.frankheijden.insights.api.exceptions;

import dev.frankheijden.insights.api.objects.chunk.ChunkCuboid;

public class ChunkCuboidOutOfBoundsException extends RuntimeException {

    public ChunkCuboidOutOfBoundsException(ChunkCuboid cuboid, ChunkCuboid max) {
        super("Cuboid '" + cuboid + "' is out of bounds for max cuboid '" + max + "'");
    }
}
