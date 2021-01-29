package dev.frankheijden.insights.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

public class PlayerUtils {

    public static Block getTargetBlock(Player player, int range) {
        BlockIterator iter = new BlockIterator(player, range);
        Block lastBlock = iter.next();
        while (iter.hasNext()) {
            lastBlock = iter.next();
            if (lastBlock.getType() == Material.AIR) {
                continue;
            }
            break;
        }
        return lastBlock;
    }

    public static Entity getTargetEntity(Player player, int range) {
        for (Entity entity : player.getNearbyEntities(range, range, range)) {
            if (isLookingAt(player, entity)) {
                return entity;
            }
        }
        return null;
    }

    public static boolean isLookingAt(Player player, Entity entity) {
        Location eye = player.getEyeLocation();
        Vector toEntity = entity.getBoundingBox().getCenter().subtract(eye.toVector());
        double dotProduct = toEntity.normalize().dot(eye.getDirection());
        return dotProduct > 0.99D;
    }
}
