package net.frankheijden.insights.utils;

import net.frankheijden.insights.managers.NMSManager;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

public class BlockUtils {

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
