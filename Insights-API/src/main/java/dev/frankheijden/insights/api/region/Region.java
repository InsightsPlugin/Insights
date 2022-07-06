package dev.frankheijden.insights.api.region;

import dev.frankheijden.insights.api.objects.chunk.ChunkPart;
import org.checkerframework.checker.nullness.qual.NonNull;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public abstract class Region {

    private List<ChunkPart> cachedChunkParts = null;

    public abstract @NonNull UUID worldUuid();

    public abstract @NonNull UUID regionUuid();

    public abstract @NonNull String name();

    public abstract @NonNull List<ChunkPart> generateChunkParts();

    public final @NonNull List<ChunkPart> chunkParts() {
        if (cachedChunkParts == null) cachedChunkParts = generateChunkParts();
        return cachedChunkParts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Region that = (Region) o;
        return worldUuid().equals(that.worldUuid()) && regionUuid().equals(that.regionUuid());
    }

    @Override
    public int hashCode() {
        return Objects.hash(worldUuid(), regionUuid());
    }
}
