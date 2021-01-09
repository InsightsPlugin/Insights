package net.frankheijden.insights.utils;

import dev.frankheijden.minecraftreflection.ClassObject;
import dev.frankheijden.minecraftreflection.MinecraftReflection;
import net.frankheijden.insights.managers.NMSManager;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import java.util.Set;

public class BlockUtils {

    private static final MinecraftReflection craftBlockStateReflection = MinecraftReflection.of("org.bukkit.craftbukkit.%s.block.CraftBlockState");
    private static final MinecraftReflection blockPositionReflection = MinecraftReflection.of("net.minecraft.server.%s.BlockPosition");
    private static final MinecraftReflection craftWorldReflection = MinecraftReflection.of("org.bukkit.craftbukkit.%s.CraftWorld");
    private static final MinecraftReflection worldReflection = MinecraftReflection.of("net.minecraft.server.%s.World");
    private static final MinecraftReflection nbtTagCompoundReflection = MinecraftReflection.of("net.minecraft.server.%s.NBTTagCompound");
    private static final MinecraftReflection tileEntityReflection = MinecraftReflection.of("net.minecraft.server.%s.TileEntity");

    public static boolean hasAnyNBTTags(Block block, Set<String> tags) {
        Object nbt = getNBT(block);
        for (String tag : tags) {
            if (nbtTagCompoundReflection.invoke(nbt, "hasKey", tag)) {
                return true;
            }
        }
        return false;
    }

    public static Object getNBT(Block block) {
        if (block == null) return false;

        BlockState state = block.getState();
        if (!craftBlockStateReflection.getClazz().isInstance(state)) return false;

        Location loc = block.getLocation();
        Object blockPosition = blockPositionReflection.newInstance(
                ClassObject.of(int.class, loc.getBlockX()),
                ClassObject.of(int.class, loc.getBlockY()),
                ClassObject.of(int.class, loc.getBlockZ())
        );

        Object world = craftWorldReflection.invoke(loc.getWorld(), "getHandle");
        Object tile = worldReflection.invoke(world, "getTileEntity", blockPosition);

        Object tag = nbtTagCompoundReflection.newInstance();
        tileEntityReflection.invoke(tile, "save", tag);

        return tag;
    }

    public static void set(Block block, BlockState state) {
        block.setType(state.getType());
        if (NMSManager.getInstance().isPost(13)) {
            block.setBlockData(state.getBlockData());
        } else {
            try {
                byte rawData = (byte) BlockState.class.getDeclaredMethod("getRawData").invoke(state);
                Block.class.getDeclaredMethod("setData", byte.class, boolean.class).invoke(block, rawData, false);
            } catch (ReflectiveOperationException ex) {
                ex.printStackTrace();
            }
        }
    }
}
