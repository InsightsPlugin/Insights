package dev.frankheijden.insights.api.reflection;

import dev.frankheijden.minecraftreflection.MinecraftReflection;
import dev.frankheijden.minecraftreflection.Reflection;
import net.minecraft.world.level.block.Block;
import org.bukkit.Material;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public class RCraftMagicNumbers {

    private static final MinecraftReflection reflection = MinecraftReflection
            .of("org.bukkit.craftbukkit.%s.util.CraftMagicNumbers");
    private static MethodHandle getMaterialMethodHandle;

    static {
        try {
            getMaterialMethodHandle = MethodHandles.lookup().unreflect(Reflection.getAccessibleMethod(
                    reflection.getClazz(),
                    "getMaterial",
                    Block.class
            ));
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    private RCraftMagicNumbers() {}

    public static MinecraftReflection getReflection() {
        return reflection;
    }

    public static Material getMaterial(Object block) throws Throwable {
        return (Material) getMaterialMethodHandle.invoke(block);
    }
}
