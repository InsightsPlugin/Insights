package dev.frankheijden.insights.api.reflection;

import dev.frankheijden.minecraftreflection.MinecraftReflection;

/**
 * Deprecated.
 * @deprecated Removal in v6.5.0
 */
@Deprecated(since = "v6.4.3")
public class RChunk {

    private static final MinecraftReflection reflection = MinecraftReflection
            .of("net.minecraft.world.level.chunk.Chunk");

    private RChunk() {}

    public static MinecraftReflection getReflection() {
        return reflection;
    }
}
