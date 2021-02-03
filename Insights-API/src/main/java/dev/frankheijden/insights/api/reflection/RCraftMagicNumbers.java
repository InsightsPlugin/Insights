package dev.frankheijden.insights.api.reflection;

import dev.frankheijden.minecraftreflection.MinecraftReflection;

public class RCraftMagicNumbers {

    private static final MinecraftReflection reflection = MinecraftReflection
            .of("org.bukkit.craftbukkit.%s.util.CraftMagicNumbers");

    private RCraftMagicNumbers() {}

    public static MinecraftReflection getReflection() {
        return reflection;
    }
}
