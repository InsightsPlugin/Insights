package dev.frankheijden.insights.api.reflection;

import dev.frankheijden.minecraftreflection.MinecraftReflection;
import dev.frankheijden.minecraftreflection.Reflection;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public class RUnsafeList {

    private static final MinecraftReflection reflection = MinecraftReflection
            .of("org.bukkit.craftbukkit.%s.util.UnsafeList");
    private static MethodHandle dataMethodHandle;
    private static MethodHandle sizeMethodHandle;

    static {
        try {
            dataMethodHandle = MethodHandles.lookup()
                    .unreflectGetter(Reflection.getAccessibleField(reflection.getClazz(), "data"));
            sizeMethodHandle = MethodHandles.lookup()
                    .unreflect(Reflection.getAccessibleMethod(reflection.getClazz(), "size"));
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    private RUnsafeList() {}

    public static Object[] getData(Object unsafeList) throws Throwable {
        return (Object[]) dataMethodHandle.invoke(unsafeList);
    }

    public static int size(Object unsafeList) throws Throwable {
        return (int) sizeMethodHandle.invoke(unsafeList);
    }
}
