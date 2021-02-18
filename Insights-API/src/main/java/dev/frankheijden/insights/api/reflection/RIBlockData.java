package dev.frankheijden.insights.api.reflection;

import dev.frankheijden.minecraftreflection.MinecraftReflection;
import dev.frankheijden.minecraftreflection.Reflection;
import org.bukkit.Material;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public class RIBlockData {

    private static final MinecraftReflection reflection = MinecraftReflection
            .of("net.minecraft.server.%s.IBlockData");
    private static MethodHandle getBukkitMaterialMethodHandle;

    static {
        try {
            getBukkitMaterialMethodHandle = MethodHandles.lookup().unreflect(Reflection.getAccessibleMethod(
                    reflection.getClazz(),
                    "getBukkitMaterial"
            ));
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    private RIBlockData() {}

    public static Material getBukkitMaterial(Object blockData) throws Throwable {
        return (Material) getBukkitMaterialMethodHandle.invoke(blockData);
    }
}
