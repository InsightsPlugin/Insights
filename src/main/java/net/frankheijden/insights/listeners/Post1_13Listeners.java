package net.frankheijden.insights.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPlaceEvent;

public class Post1_13Listeners implements Listener {
    private MainListener mainListener;

    public Post1_13Listeners(MainListener mainListener) {
        this.mainListener = mainListener;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityPlace(EntityPlaceEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;
        mainListener.handleEntityPlace(event, player, event.getEntity().getLocation().getChunk(), event.getEntityType().name());
    }
}