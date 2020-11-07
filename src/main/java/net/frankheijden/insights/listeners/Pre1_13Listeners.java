package net.frankheijden.insights.listeners;

import net.frankheijden.insights.Insights;
import net.frankheijden.insights.events.PlayerEntityPlaceEvent;
import net.frankheijden.insights.utils.EntityUtils;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class Pre1_13Listeners implements Listener {
    private final MainListener mainListener;

    private final boolean vehicleCancellable;

    public Pre1_13Listeners(MainListener mainListener) {
        this.mainListener = mainListener;
        this.vehicleCancellable = VehicleCreateEvent.class.isAssignableFrom(Cancellable.class);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVehicleCreate(VehicleCreateEvent event) {
        Vehicle vehicle = event.getVehicle();
        Player player = mainListener.getInteractListener().getPlayerWithinRadius(vehicle.getLocation());

        if (player != null) {
            if (vehicleCancellable) {
                EntityListener.handleEntityPlaceEvent(event, player, vehicle);
            } else {
                handleVehiclePlace(player, vehicle);
            }
        }
    }

    private void handleVehiclePlace(Player player, Vehicle vehicle) {
        // Handle vehicle placements in runnable because the event is not reliable
        // when knowing if an entity is actually placed on the ground.
        new BukkitRunnable() {
            @Override
            public void run() {
                if (vehicle.isValid()) {
                    PlayerEntityPlaceEvent entityPlaceEvent = new PlayerEntityPlaceEvent(player, vehicle);
                    Bukkit.getPluginManager().callEvent(entityPlaceEvent);

                    if (entityPlaceEvent.isCancelled()) {
                        vehicle.remove();
                        if (player.getGameMode() == GameMode.CREATIVE) return;
                        player.getInventory().addItem(EntityUtils.createItemStack(vehicle, 1));
                    }
                }
            }
        }.runTaskLater(Insights.getInstance(), 1);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent event) {
        Entity entity = event.getEntity();
        Player player = mainListener.getInteractListener().getPlayerWithinRadius(entity.getLocation());

        if (player != null) {
            EntityListener.handleEntityPlaceEvent(event, player, entity);
        }
    }
}
