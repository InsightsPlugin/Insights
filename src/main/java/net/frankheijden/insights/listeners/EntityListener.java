package net.frankheijden.insights.listeners;

import net.frankheijden.insights.events.PlayerEntityPlaceEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;

public class EntityListener implements Listener {

    private final MainListener listener;

    public EntityListener(MainListener listener) {
        this.listener = listener;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getEntity();

        handleEntityEvent(event, player, entity);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        Entity entity = event.getEntity();
        Player player = listener.getInteractListener().getPlayerWithinRadius(entity.getLocation());

        if (player != null) {
            handleEntityEvent(event, player, entity);
        }
    }

    public static void handleEntityEvent(Cancellable cancellable, Player player, Entity entity) {
        PlayerEntityPlaceEvent entityPlaceEvent = new PlayerEntityPlaceEvent(player, entity);
        Bukkit.getPluginManager().callEvent(entityPlaceEvent);
        cancellable.setCancelled(entityPlaceEvent.isCancelled());
    }
}
