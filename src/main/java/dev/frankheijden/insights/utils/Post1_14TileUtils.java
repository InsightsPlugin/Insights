package dev.frankheijden.insights.utils;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;

public class Post1_14TileUtils {
    public static boolean isTile(Block block) {
        BlockState blockState = block.getState();
        return isTile(blockState);
    }

    public static boolean isTile(BlockState blockState) {
        return blockState instanceof TileState;
    }
}
