package dev.frankheijden.insights.api.addons;

import dev.frankheijden.insights.api.region.Region;
import dev.frankheijden.insights.api.objects.chunk.ChunkPart;
import org.checkerframework.checker.nullness.qual.NonNull;
import java.util.List;
import java.util.UUID;

public class AddonRegion extends Region {

    private final String addonId;
    private final Region region;

    public AddonRegion(@NonNull String addonId, @NonNull Region region) {
        this.addonId = addonId;
        this.region = region;
    }


    public @NonNull String addonId() {
        return addonId;
    }

    @Override
    public @NonNull UUID worldUuid() {
        return region.worldUuid();
    }

    @Override
    public @NonNull UUID regionUuid() {
        return region.regionUuid();
    }

    @Override
    public @NonNull String name() {
        return region.name();
    }

    @Override
    public @NonNull List<ChunkPart> generateChunkParts() {
        return region.generateChunkParts();
    }
}
