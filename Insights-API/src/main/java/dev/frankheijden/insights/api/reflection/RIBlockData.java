package dev.frankheijden.insights.api.reflection;

import dev.frankheijden.minecraftreflection.MinecraftReflection;
import dev.frankheijden.minecraftreflection.Reflection;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public class RIBlockData {

    private static final MinecraftReflection reflection = MinecraftReflection
            .of("net.minecraft.server.%s.IBlockData");
    private static MethodHandle getBlockMethodHandle;

    static {
        try {
            getBlockMethodHandle = MethodHandles.lookup().unreflect(Reflection.getAccessibleMethod(
                    reflection.getClazz(),
                    "getBlock"
            ));
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    private RIBlockData() {}

    public static Object getBlock(Object blockData) throws Throwable {
        return getBlockMethodHandle.invoke(blockData);
    }
}
