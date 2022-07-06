package dev.frankheijden.insights.api.region;

import org.checkerframework.checker.nullness.qual.NonNull;
import java.util.List;
import java.util.UUID;

public class SimpleMultiCuboidRegion extends MultiCuboidRegion {

    protected final UUID worldUuid;
    protected final UUID regionUuid;
    protected final String name;

    /**
     * Constructs a new multi cuboid (simply a list of cuboids).
     */
    public SimpleMultiCuboidRegion(
            @NonNull List<CuboidRegion> cuboidRegions,
            @NonNull UUID worldUuid,
            @NonNull UUID regionUuid,
            @NonNull String name
    ) {
        super(cuboidRegions);
        this.worldUuid = worldUuid;
        this.regionUuid = regionUuid;
        this.name = name;
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
        return name;
    }
}
