package dev.frankheijden.insights.listeners;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPlaceEvent;

public class Post1_13Listeners implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityPlace(EntityPlaceEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getEntity();
        if (player == null) return;

        EntityListener.handleEntityPlaceEvent(event, player, entity, false);
    }

    public static Block getOther(Block block) {
        if (block.getBlockData() instanceof Bed) {
            return getOther(block, (Bed) block.getBlockData());
        } else if (block.getBlockData() instanceof Door) {
            return getOther(block, (Door) block.getBlockData());
        }
        return null;
    }

    public static Block getOther(Block block, Bed bed) {
        if (bed.getPart() == Bed.Part.HEAD) {
            return block.getRelative(bed.getFacing().getOppositeFace());
        }
        return block.getRelative(bed.getFacing());
    }

    public static Block getOther(Block block, Door door) {
        if (door.getHalf() == Bisected.Half.BOTTOM) {
            return block.getRelative(BlockFace.UP);
        }
        return block.getRelative(BlockFace.DOWN);
    }
}
