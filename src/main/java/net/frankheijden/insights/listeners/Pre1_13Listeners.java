package net.frankheijden.insights.listeners;

import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;

public class Pre1_13Listeners implements Listener {
    private final MainListener mainListener;

    public Pre1_13Listeners(MainListener mainListener) {
        this.mainListener = mainListener;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVehicleCreate(VehicleCreateEvent event) {
        Vehicle vehicle = event.getVehicle();
        Player player = mainListener.getInteractListener().getPlayerWithinRadius(vehicle.getLocation());

        if (player != null) {
            mainListener.handleEntityPlace(event, player, vehicle.getLocation().getChunk(), vehicle.getType().name());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent event) {
        Entity entity = event.getEntity();
        Player player = mainListener.getInteractListener().getPlayerWithinRadius(entity.getLocation());

        if (player != null) {
            mainListener.handleEntityPlace(event, player, entity.getLocation().getChunk(), entity.getType().name());
        }
    }
}
