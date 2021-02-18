package dev.frankheijden.insights.api.nms;

import org.bukkit.Material;

public interface NMSChunkSection {

    boolean isEmpty();

    Material getType(int x, int y, int z);

}
