package dev.frankheijden.insights.api.addons;

import dev.frankheijden.insights.api.objects.chunk.ChunkPart;
import dev.frankheijden.insights.api.objects.math.Cuboid;
import java.util.List;
import java.util.stream.Collectors;

public abstract class MultiCuboidRegion implements Region {

    protected final List<Cuboid> cuboids;

    protected MultiCuboidRegion(List<Cuboid> cuboids) {
        this.cuboids = cuboids;
    }

    @Override
    public List<ChunkPart> toChunkParts() {
        return cuboids.stream().flatMap(cuboid -> cuboid.toChunkParts().stream()).collect(Collectors.toList());
    }
}
