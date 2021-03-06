package dev.frankheijden.insights.api.reflection;

import dev.frankheijden.minecraftreflection.MinecraftReflection;

public class RBlock {

    private static final MinecraftReflection reflection = MinecraftReflection.of("net.minecraft.server.%s.Block");

    private RBlock() {}

    public static MinecraftReflection getReflection() {
        return reflection;
    }
}
