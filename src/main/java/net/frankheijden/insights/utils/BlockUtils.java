package net.frankheijden.insights.utils;

import dev.frankheijden.minecraftreflection.ClassObject;
import dev.frankheijden.minecraftreflection.MinecraftReflection;
import java.util.Map;
import net.frankheijden.insights.managers.NMSManager;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

public class BlockUtils {

    private static final MinecraftReflection craftBlockStateReflection = MinecraftReflection.of("org.bukkit.craftbukkit.%s.block.CraftBlockState");
    private static final MinecraftReflection blockPositionReflection = MinecraftReflection.of("net.minecraft.server.%s.BlockPosition");
    private static final MinecraftReflection craftWorldReflection = MinecraftReflection.of("org.bukkit.craftbukkit.%s.CraftWorld");
    private static final MinecraftReflection worldReflection = MinecraftReflection.of("net.minecraft.server.%s.World");
    private static final MinecraftReflection nbtTagCompoundReflection = MinecraftReflection.of("net.minecraft.server.%s.NBTTagCompound");
    private static final MinecraftReflection tileEntityReflection = MinecraftReflection.of("net.minecraft.server.%s.TileEntity");

    public static boolean hasAnyNBTTags(Block block, Map<String, String> tags) {
        Object nbt = getNBT(block);
        if (nbt == null) return false;

        for (Map.Entry<String, String> entry : tags.entrySet()) {
            if (nbtTagCompoundReflection.invoke(nbt, "hasKey", entry.getKey())) {
                String value = entry.getValue();
                if (value == null || value.equals(nbtTagCompoundReflection.invoke(nbt, "getString", entry.getKey()))) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Map<String, Object> getNBTAsMap(Block block) {
        Object nbt = getNBT(block);
        if (nbt == null) return null;
        return nbtTagCompoundReflection.get(nbt, "map");
    }

    public static Object getNBT(Block block) {
        if (block == null) return null;

        BlockState state = block.getState();
        if (!craftBlockStateReflection.getClazz().isInstance(state)) return null;

        Location loc = block.getLocation();
        Object blockPosition = blockPositionReflection.newInstance(
                ClassObject.of(int.class, loc.getBlockX()),
                ClassObject.of(int.class, loc.getBlockY()),
                ClassObject.of(int.class, loc.getBlockZ())
        );

        Object world = craftWorldReflection.invoke(loc.getWorld(), "getHandle");
        Object tile = worldReflection.invoke(world, "getTileEntity", blockPosition);
        if (tile == null) return null;

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
