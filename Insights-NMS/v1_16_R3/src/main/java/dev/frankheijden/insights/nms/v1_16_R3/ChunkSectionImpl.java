package dev.frankheijden.insights.nms.v1_16_R3;

import dev.frankheijden.insights.api.nms.NMSChunkSection;
import net.minecraft.server.v1_16_R3.ChunkSection;
import org.bukkit.Material;

public class ChunkSectionImpl implements NMSChunkSection {

    private final ChunkSection section;

    public ChunkSectionImpl(ChunkSection section) {
        this.section = section;
    }

    @Override
    public boolean isEmpty() {
        return ChunkSection.a(section);
    }

    @Override
    public Material getType(int x, int y, int z) {
        return section.getType(x, y, z).getBukkitMaterial();
    }
}
