package net.frankheijden.insights.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityPlaceEvent;

public class Post1_13Listeners implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityPlace(EntityPlaceEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getEntity();
        if (player == null) return;

        EntityListener.handleEntityPlaceEvent(event, player, entity);
    }
}
