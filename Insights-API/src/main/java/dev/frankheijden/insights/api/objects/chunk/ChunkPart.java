package dev.frankheijden.insights.api.objects.chunk;

import java.util.Objects;

public class ChunkPart {

    private final ChunkLocation chunkLocation;
    private final ChunkCuboid chunkCuboid;

    public ChunkPart(ChunkLocation chunkLocation) {
        this(chunkLocation, ChunkCuboid.maxCuboid(chunkLocation.getWorld()));
    }

    public ChunkPart(ChunkLocation chunkLocation, ChunkCuboid chunkCuboid) {
        this.chunkLocation = chunkLocation;
        this.chunkCuboid = chunkCuboid;
    }

    public ChunkLocation getChunkLocation() {
        return chunkLocation;
    }

    public ChunkCuboid getChunkCuboid() {
        return chunkCuboid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkPart chunkPart = (ChunkPart) o;
        return chunkLocation.equals(chunkPart.chunkLocation) && chunkCuboid.equals(chunkPart.chunkCuboid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chunkLocation, chunkCuboid);
    }
}
