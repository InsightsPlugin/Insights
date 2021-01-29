package dev.frankheijden.insights.listeners;

import dev.frankheijden.insights.Insights;
import dev.frankheijden.insights.events.PlayerEntityPlaceEvent;
import dev.frankheijden.insights.utils.EntityUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
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
                EntityListener.handleEntityPlaceEvent(event, player, vehicle, true);
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
            EntityListener.handleEntityPlaceEvent(event, player, entity, true);
        }
    }
}
