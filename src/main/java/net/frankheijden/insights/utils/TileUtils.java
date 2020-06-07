package net.frankheijden.insights.utils;

import net.frankheijden.insights.managers.NMSManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class TileUtils {

    private static Set<String> tiles;

    public TileUtils(File file) {
        tiles = new HashSet<>(YamlConfiguration.loadConfiguration(file).getStringList("tiles"));
    }

    public static Set<String> getTiles() {
        return tiles;
    }

    public static boolean isTile(Material material) {
        return tiles.contains(material.name());
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
