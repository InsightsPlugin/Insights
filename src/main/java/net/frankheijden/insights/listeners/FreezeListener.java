package net.frankheijden.insights.listeners;

import net.frankheijden.insights.Insights;
import net.frankheijden.insights.managers.FreezeManager;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class FreezeListener implements Listener {

    private static final Insights plugin = Insights.getInstance();
    private static final FreezeManager freezeManager = FreezeManager.getInstance();

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        handleEvent(event, event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        handleEvent(event, event.getPlayer());
    }

    public static void handleEvent(Cancellable event, Player player) {
        if (!freezeManager.isFrozen(player.getUniqueId())) return;
        event.setCancelled(true);
        player.sendMessage("Brrrrrr!");
    }
}
