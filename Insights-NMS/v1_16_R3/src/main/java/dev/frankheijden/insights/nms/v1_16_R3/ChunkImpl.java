package dev.frankheijden.insights.nms.v1_16_R3;

import dev.frankheijden.insights.api.nms.NMSChunk;
import dev.frankheijden.insights.api.nms.NMSChunkSection;
import net.minecraft.server.v1_16_R3.ChunkSection;
import org.bukkit.Chunk;
import org.bukkit.craftbukkit.v1_16_R3.CraftChunk;

public class ChunkImpl extends NMSChunk {

    protected ChunkImpl(Chunk chunk) {
        super(chunk);
    }

    @Override
    public NMSChunkSection[] getSections() {
        ChunkSection[] sections = ((CraftChunk) chunk).getHandle().getSections();
        NMSChunkSection[] wraps = new NMSChunkSection[sections.length];
        for (int i = 0; i < wraps.length; i++) {
            wraps[i] = new ChunkSectionImpl(sections[i]);
        }
        return wraps;
    }
}
