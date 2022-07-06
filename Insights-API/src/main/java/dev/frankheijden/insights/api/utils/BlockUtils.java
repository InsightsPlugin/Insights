package dev.frankheijden.insights.api.utils;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Bed;
import java.util.Optional;

public class BlockUtils {

    private BlockUtils() {}

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
