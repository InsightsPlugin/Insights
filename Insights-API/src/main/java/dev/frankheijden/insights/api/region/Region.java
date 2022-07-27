package dev.frankheijden.insights.api.region;

import dev.frankheijden.insights.api.objects.chunk.ChunkPart;
import org.checkerframework.checker.nullness.qual.NonNull;
import java.util.List;
import java.util.UUID;

public interface Region {

    @NonNull UUID worldUuid();

    @NonNull UUID regionUuid();

    @NonNull String name();

    @NonNull List<ChunkPart> generateChunkParts();
}
