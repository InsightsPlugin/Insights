package dev.frankheijden.insights.api.addons;

import dev.frankheijden.insights.api.region.Region;
import dev.frankheijden.insights.api.objects.chunk.ChunkPart;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import java.util.List;
import java.util.UUID;

public class AddonRegion implements Region {

    private final @NonNull InsightsAddon addon;
    private final @NonNull Region region;
    private final @Nullable String areaName;

    public AddonRegion(
            @NonNull InsightsAddon addon,
            @NonNull Region region
    ) {
        this(addon, region, null);
    }

    /**
     * Constructs the AddonRegion decorator class.
     */
    public AddonRegion(
            @NonNull InsightsAddon addon,
            @NonNull Region region,
            @Nullable String areaName
    ) {
        this.addon = addon;
        this.region = region;
        this.areaName = areaName;
    }

    public @NonNull InsightsAddon addon() {
        return addon;
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

    public @Nullable String areaName() {
        return areaName;
    }
}
