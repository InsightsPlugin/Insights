package net.frankheijden.insights.utils;

import org.bukkit.block.*;

public class Post1_13TileUtils {
    public static boolean isTile(Block block) {
        BlockState blockState = block.getState();
        return isTile(blockState);
    }

    public static boolean isTile(BlockState blockState) {
        return blockState instanceof TileState;
    }
}
