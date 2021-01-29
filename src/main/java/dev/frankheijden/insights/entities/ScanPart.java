package dev.frankheijden.insights.entities;

import org.bukkit.Chunk;

public class ScanPart {

    private final Chunk chunk;
    private final PartialChunk partial;

    public ScanPart(Chunk chunk, PartialChunk partial) {
        this.chunk = chunk;
        this.partial = partial;
    }

    public Chunk getChunk() {
        return chunk;
    }

    public PartialChunk getPartialChunk() {
        return partial;
    }
}
