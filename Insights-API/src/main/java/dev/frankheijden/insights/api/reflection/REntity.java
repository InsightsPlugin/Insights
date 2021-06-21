package dev.frankheijden.insights.api.reflection;

import dev.frankheijden.minecraftreflection.MinecraftReflection;
import dev.frankheijden.minecraftreflection.Reflection;
import org.bukkit.entity.Entity;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public class REntity {

    private static final MinecraftReflection reflection = MinecraftReflection.of("net.minecraft.world.entity.Entity");
    private static MethodHandle getBukkitEntityMethodHandle;
    private static MethodHandle locXMethodHandle;
    private static MethodHandle locYMethodHandle;
    private static MethodHandle locZMethodHandle;

    static {
        try {
            getBukkitEntityMethodHandle = get("getBukkitEntity");
            locXMethodHandle = get("locX");
            locYMethodHandle = get("locY");
            locZMethodHandle = get("locZ");
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    private static MethodHandle get(String method) throws IllegalAccessException {
        return MethodHandles.lookup().unreflect(Reflection.getAccessibleMethod(reflection.getClazz(), method));
    }

    private REntity() {}

    public static Entity getBukkitEntity(Object entity) throws Throwable {
        return (Entity) getBukkitEntityMethodHandle.invoke(entity);
    }

    public static double locX(Object entity) throws Throwable {
        return (double) locXMethodHandle.invoke(entity);
    }

    public static double locY(Object entity) throws Throwable {
        return (double) locYMethodHandle.invoke(entity);
    }

    public static double locZ(Object entity) throws Throwable {
        return (double) locZMethodHandle.invoke(entity);
    }
}
