package dev.frankheijden.insights.api.addons;

import dev.frankheijden.insights.api.objects.chunk.ChunkPart;
import java.util.List;

public interface Region {

    String getAddon();

    String getKey();

    List<ChunkPart> toChunkParts();
}
