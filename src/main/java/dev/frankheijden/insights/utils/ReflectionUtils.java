package dev.frankheijden.insights.utils;

import dev.frankheijden.insights.entities.AddonError;
import dev.frankheijden.insights.entities.Error;
import dev.frankheijden.insights.managers.NMSManager;
import dev.frankheijden.insights.entities.CacheAssistant;
import org.bukkit.World;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

public class ReflectionUtils {

    private static Class<?> craftWorldClass;
    private static Method craftWorldGetHandle;
    private static Class<?> blockPositionClass;
    private static Constructor<?> newBlockPosition;
    private static Class<?> worldServerClass;
    private static Method getTileEntity;
    static {
        try {
            craftWorldClass = Class.forName("org.bukkit.craftbukkit." + NMSManager.NMS + ".CraftWorld");
            craftWorldGetHandle = craftWorldClass.getDeclaredMethod("getHandle");
            blockPositionClass = Class.forName("net.minecraft.server." + NMSManager.NMS + ".BlockPosition");
            newBlockPosition = blockPositionClass.getDeclaredConstructor(double.class, double.class, double.class);
            worldServerClass = Class.forName("net.minecraft.server." + NMSManager.NMS + ".WorldServer");
            getTileEntity = worldServerClass.getMethod("getTileEntity", blockPositionClass);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public static Object getWorldServer(World world) throws Exception {
        Object craftWorld = craftWorldClass.cast(world);
        return craftWorldGetHandle.invoke(craftWorld);
    }

    public static Object createBlockPosition(double x, double y, double z) throws Exception {
        return newBlockPosition.newInstance(x, y, z);
    }

    public static Object getTileEntity(Object worldServer, Object blockPosition) throws Exception {
        return getTileEntity.invoke(worldServer, blockPosition);
    }

    public static CacheAssistant createCacheAssistant(List<Error> errors, Class<?> clazz) {
        if (clazz == null || !CacheAssistant.class.isAssignableFrom(clazz)) {
            return null;
        }

        CacheAssistant assistant = null;
        try {
            Constructor<?>[] c = clazz.getConstructors();
            if (c.length == 0) {
                assistant = (CacheAssistant) clazz.newInstance();
            } else {
                for (Constructor<?> con : c) {
                    if (con.getParameterTypes().length == 0) {
                        assistant = (CacheAssistant) clazz.newInstance();
                        break;
                    }
                }
            }
        } catch (Throwable th) {
            errors.add(new AddonError("Failed to initialise addon: " + clazz.getName()));
            th.printStackTrace();
        }

        return assistant;
    }
}
