package dev.frankheijden.insights.api.utils;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Bed;
import java.util.Optional;

public class BlockUtils {

    private BlockUtils() {}

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
        var data = block.getBlockData();
        if (data instanceof Bed) {
            var bed = (Bed) data;
            BlockFace facing = bed.getFacing();
            return Optional.of(block.getRelative(bed.getPart() == Bed.Part.HEAD ? facing.getOppositeFace() : facing));
        }
        return Optional.empty();
    }
}
