package dev.frankheijden.insights.api.utils;

import dev.frankheijden.insights.api.reflection.RTileEntityTypes;
import dev.frankheijden.minecraftreflection.ClassObject;
import dev.frankheijden.minecraftreflection.MinecraftReflection;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bed;
import java.util.Optional;

public class BlockUtils {

    private static final MinecraftReflection craftBlockStateReflection = MinecraftReflection
            .of("org.bukkit.craftbukkit.%s.block.CraftBlockState");
    private static final MinecraftReflection blockPositionReflection = MinecraftReflection
            .of("net.minecraft.server.%s.BlockPosition");
    private static final MinecraftReflection craftWorldReflection = MinecraftReflection
            .of("org.bukkit.craftbukkit.%s.CraftWorld");
    private static final MinecraftReflection worldReflection = MinecraftReflection
            .of("net.minecraft.server.%s.World");
    private static final MinecraftReflection nbtTagCompoundReflection = MinecraftReflection
            .of("net.minecraft.server.%s.NBTTagCompound");
    private static final MinecraftReflection tileEntityReflection = MinecraftReflection
            .of("net.minecraft.server.%s.TileEntity");

    private BlockUtils() {}

    public static boolean isTileEntity(Material m) {
        return RTileEntityTypes.isTileEntity(m);
    }

    public static boolean isTileEntity(Location loc) {
        return getTileEntity(loc) != null;
    }

    public static boolean isTileEntity(Block block) {
        return isTileEntity(block.getState());
    }

    public static boolean isTileEntity(BlockState state) {
        return craftBlockStateReflection.getClazz().isInstance(state);
    }

    public static Object getTileEntity(Location loc) {
        Object nmsWorld = craftWorldReflection.invoke(loc.getWorld(), "getHandle");
        return getTileEntity(nmsWorld, createBlockPosition(loc));
    }

    public static Object getTileEntity(Object nmsWorld, Object blockPosition) {
        return worldReflection.invoke(nmsWorld, "getTileEntity", blockPosition);
    }

    public static Object createBlockPosition(Location loc) {
        return createBlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    /**
     * Creates a new BlockPosition from given x, y, z coordinates.
     */
    public static Object createBlockPosition(int x, int y, int z) {
        return blockPositionReflection.newInstance(
                ClassObject.of(int.class, x),
                ClassObject.of(int.class, y),
                ClassObject.of(int.class, z)
        );
    }

    /**
     * Checks whether two blocks are within the same chunk.
     */
    public static boolean isSameChunk(Block x, Block y) {
        return ((x.getX() >> 4) == (y.getX() >> 4)) && (x.getZ() >> 4) == (y.getZ() >> 4);
    }

    /**
     * Checks whether two x,z block location pairs are within the same chunk.
     */
    public static boolean isSameChunk(int x1, int z1, int x2, int z2) {
        return ((x1 >> 4) == (x2 >> 4)) && (z1 >> 4) == (z2 >> 4);
    }

    /**
     * Attempts to retrieve the other half of a block.
     */
    public static Optional<Block> getOtherHalf(Block block) {
        BlockData data = block.getBlockData();
        if (data instanceof Bed) {
            Bed bed = (Bed) data;
            BlockFace facing = bed.getFacing();
            return Optional.of(block.getRelative(bed.getPart() == Bed.Part.HEAD ? facing.getOppositeFace() : facing));
        }
        return Optional.empty();
    }
}
