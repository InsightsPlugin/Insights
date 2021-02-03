package dev.frankheijden.insights.api.reflection;

import dev.frankheijden.minecraftreflection.MinecraftReflection;

public class RItem {

    private static final MinecraftReflection reflection = MinecraftReflection.of("net.minecraft.server.%s.Item");

    private RItem() {}

    public static MinecraftReflection getReflection() {
        return reflection;
    }
}
