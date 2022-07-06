package dev.frankheijden.insights.api.region;

import dev.frankheijden.insights.api.objects.chunk.ChunkPart;
import org.checkerframework.checker.nullness.qual.NonNull;
import java.util.List;
import java.util.stream.Collectors;

public abstract class MultiCuboidRegion extends Region {

    protected final List<CuboidRegion> cuboids;

    protected MultiCuboidRegion(List<CuboidRegion> cuboids) {
        this.cuboids = cuboids;
    }

    @Override
    public @NonNull List<ChunkPart> generateChunkParts() {
        return cuboids.stream().flatMap(cuboid -> cuboid.generateChunkParts().stream()).collect(Collectors.toList());
    }
}
