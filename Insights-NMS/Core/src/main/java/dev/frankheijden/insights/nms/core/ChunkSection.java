package dev.frankheijden.insights.nms.core;

import org.bukkit.Material;
import java.util.function.BiConsumer;

public interface ChunkSection {

    int index();

    Material blockAt(int x, int y, int z);

    void countBlocks(BiConsumer<Material, Integer> consumer);

}
