package dev.frankheijden.insights.utils;

import dev.frankheijden.insights.managers.NMSManager;
import dev.frankheijden.insights.managers.TileManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Set;

public class TileUtils {

    private static final TileManager tileManager = TileManager.getInstance();

    public static Set<Material> getTiles() {
        return tileManager.getTiles();
    }

    public static boolean isTile(Material material) {
        return tileManager.isTile(material);
    }

    public static boolean isTile(Block block) {
        if (NMSManager.getInstance().isPost(14)) {
            return Post1_14TileUtils.isTile(block);
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
