package dev.frankheijden.insights.api.reflection;

import dev.frankheijden.minecraftreflection.MinecraftReflection;
import dev.frankheijden.minecraftreflection.Reflection;
import org.bukkit.Material;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public class RChunkSection {

    private static final MinecraftReflection reflection = MinecraftReflection
            .of("net.minecraft.server.%s.ChunkSection");
    private static MethodHandle isEmptyMethodHandle;
    private static MethodHandle getTypeMethodHandle;

    static {
        try {
            isEmptyMethodHandle = MethodHandles.lookup().unreflect(Reflection.getAccessibleMethod(
                    reflection.getClazz(),
                    "a",
                    reflection.getClazz()
            ));
            getTypeMethodHandle = MethodHandles.lookup().unreflect(Reflection.getAccessibleMethod(
                    reflection.getClazz(),
                    "getType",
                    int.class,
                    int.class,
                    int.class
            ));
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    private RChunkSection() {}

    public static MinecraftReflection getReflection() {
        return reflection;
    }

    public static boolean isEmpty(Object chunkSection) throws Throwable {
        return (boolean) isEmptyMethodHandle.invoke(chunkSection);
    }

    public static Material getType(Object chunkSection, int x, int y, int z) throws Throwable {
        return RIBlockData.getBukkitMaterial(getTypeMethodHandle.invoke(chunkSection, x, y, z));
    }
}
