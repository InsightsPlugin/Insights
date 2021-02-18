package dev.frankheijden.insights.api.nms;

import org.bukkit.Chunk;

public abstract class NMSChunk {

    protected Chunk chunk;

    protected NMSChunk(Chunk chunk) {
        this.chunk = chunk;
    }

    public abstract NMSChunkSection[] getSections();

}
