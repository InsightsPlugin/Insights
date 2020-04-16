package net.frankheijden.insights.utils;

import net.frankheijden.insights.Insights;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public class TileUtils {

    private static final Insights plugin = Insights.getInstance();

    public static boolean isTile(Block block) {
        if (plugin.isPost1_13()) {
            return Post1_13TileUtils.isTile(block);
        }
        return isTile(block.getWorld(), block.getLocation());
    }

    public static boolean isTile(World world, Location loc) {
        return isTile(world, loc.getX(), loc.getY(), loc.getZ());
    }

    public static boolean isTile(World world, double x, double y, double z) {
        try {
            Object worldServer = ReflectionUtils.getWorldServer(world);
            Object blockPosition = ReflectionUtils.createBlockPosition(x, y, z);
            Object tileEntity = ReflectionUtils.getTileEntity(worldServer, blockPosition);
            return tileEntity != null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
