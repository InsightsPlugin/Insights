package dev.frankheijden.insights.nms.v1_16_R3;

import dev.frankheijden.insights.api.nms.NMSChunk;
import dev.frankheijden.insights.api.nms.NMSChunkFactory;
import org.bukkit.Chunk;

public class ChunkFactoryImpl implements NMSChunkFactory {

    @Override
    public NMSChunk create(Chunk chunk) {
        return new ChunkImpl(chunk);
    }
}
