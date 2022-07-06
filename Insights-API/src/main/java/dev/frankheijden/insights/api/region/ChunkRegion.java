package dev.frankheijden.insights.api.region;

import dev.frankheijden.insights.api.objects.chunk.ChunkLocation;
import dev.frankheijden.insights.api.objects.chunk.ChunkPart;
import dev.frankheijden.insights.api.utils.ChunkUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ChunkRegion extends Region {

    private final UUID worldUuid;
    private final UUID regionUuid;
    private final List<ChunkPart> chunkParts;

    /**
     * Constructs a new ChunkRegion.
     */
    public ChunkRegion(ChunkLocation chunkLocation) {
        this(
                chunkLocation.world().getUID(),
                ChunkUtils.uuidFromChunkKey(chunkLocation.key()),
                chunkLocation.toPart()
        );
    }

    /**
     * Constructs a new ChunkRegion.
     */
    public ChunkRegion(UUID worldUuid, UUID regionUuid, ChunkPart chunkPart) {
        this.worldUuid = worldUuid;
        this.regionUuid = regionUuid;
        this.chunkParts = Collections.singletonList(chunkPart);
    }

    @Override
    public @NonNull UUID worldUuid() {
        return worldUuid;
    }

    @Override
    public @NonNull UUID regionUuid() {
        return regionUuid;
    }

    @Override
    public @NonNull String name() {
        var lsb = regionUuid.getLeastSignificantBits();
        return "chunk_" + ChunkUtils.getX(lsb) + "_" + ChunkUtils.getZ(lsb);
    }

    @Override
    public @NonNull List<ChunkPart> generateChunkParts() {
        return chunkParts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkRegion that = (ChunkRegion) o;
        return worldUuid.equals(that.worldUuid)
                && regionUuid.equals(that.regionUuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(worldUuid, regionUuid);
    }
}
