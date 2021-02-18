package dev.frankheijden.insights.api.reflection;

import dev.frankheijden.minecraftreflection.MinecraftReflection;

public class RCraftChunk {

    private static final MinecraftReflection reflection = MinecraftReflection
            .of("org.bukkit.craftbukkit.%s.CraftChunk");

    private RCraftChunk() {}

    public static MinecraftReflection getReflection() {
        return reflection;
    }
}
