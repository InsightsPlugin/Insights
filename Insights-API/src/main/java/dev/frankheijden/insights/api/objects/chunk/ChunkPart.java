package dev.frankheijden.insights.api.objects.chunk;

public class ChunkPart {

    private final ChunkLocation chunkLocation;
    private final ChunkCuboid chunkCuboid;

    public ChunkPart(ChunkLocation chunkLocation) {
        this(chunkLocation, ChunkCuboid.MAX);
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
}
