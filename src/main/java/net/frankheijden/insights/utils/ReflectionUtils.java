package net.frankheijden.insights.utils;

import net.frankheijden.insights.managers.NMSManager;
import org.bukkit.World;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class ReflectionUtils {

    public static Object getWorldServer(World world) throws Exception {
        Class<?> craftWorldClass = Class.forName("org.bukkit.craftbukkit." + NMSManager.NMS + ".CraftWorld");
        Object craftWorld = craftWorldClass.cast(world);
        Method getHandle = craftWorldClass.getDeclaredMethod("getHandle");
        return getHandle.invoke(craftWorld);
    }

    public static Object createBlockPosition(double x, double y, double z) throws Exception {
        Class<?> blockPositionClass = Class.forName("net.minecraft.server." + NMSManager.NMS + ".BlockPosition");
        Constructor<?> newBlockPosition = blockPositionClass.getDeclaredConstructor(double.class, double.class, double.class);
        return newBlockPosition.newInstance(x, y, z);
    }

    public static Object getTileEntity(Object worldServer, Object blockPosition) throws Exception {
        Class<?> worldServerClass = Class.forName("net.minecraft.server." + NMSManager.NMS + ".WorldServer");
        Class<?> blockPositionClass = Class.forName("net.minecraft.server." + NMSManager.NMS + ".BlockPosition");
        Method getTileEntity = worldServerClass.getMethod("getTileEntity", blockPositionClass);
        return getTileEntity.invoke(worldServer, blockPosition);
    }

}
