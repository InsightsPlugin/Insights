package dev.frankheijden.insights.api.nms;

import org.bukkit.Chunk;

public interface NMSChunkFactory {

    NMSChunk create(Chunk chunk);

}
