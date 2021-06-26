package dev.frankheijden.insights.api.reflection;

import dev.frankheijden.minecraftreflection.MinecraftReflection;

/**
 * Deprecated.
 * @deprecated Removal in v6.5.0
 */
@Deprecated(since = "v6.4.3")
public class RBlock {

    private static final MinecraftReflection reflection = MinecraftReflection
            .of("net.minecraft.world.level.block.Block");

    private RBlock() {}

    public static MinecraftReflection getReflection() {
        return reflection;
    }
}
