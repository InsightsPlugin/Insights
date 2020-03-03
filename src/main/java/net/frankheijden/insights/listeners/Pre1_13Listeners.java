package net.frankheijden.insights.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Pre1_13Listeners implements Listener {
    private MainListener mainListener;
    private Map<Location, Player> interactLocations;
    private Map<Location, Long> interactTimes;

    public Pre1_13Listeners(MainListener mainListener) {
        this.mainListener = mainListener;
        this.interactLocations = new HashMap<>();
        this.interactTimes = new HashMap<>();
    }

    private Player getPlayerWithinRadius(Location location) {
        List<Location> locationsToRemove = new ArrayList<>();
        for (Location loc : interactLocations.keySet()) {
            if (System.currentTimeMillis() - interactTimes.get(loc) > 500) {
                locationsToRemove.add(loc);
                continue;
            }

            if (location.distance(loc) <= 1.5) {
                locationsToRemove.add(loc);
                return interactLocations.get(loc);
            }
        }

        for (Location loc : locationsToRemove) {
            interactLocations.remove(loc);
            interactTimes.remove(loc);
        }

        return null;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;

        Location loc = event.getClickedBlock().getLocation();
        interactLocations.put(loc, event.getPlayer());
        interactTimes.put(loc, System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVehicleCreate(VehicleCreateEvent event) {
        Vehicle vehicle = event.getVehicle();
        Player player = getPlayerWithinRadius(vehicle.getLocation());

        if (player != null) {
            mainListener.handleEntityPlace(event, player, vehicle.getLocation().getChunk(), vehicle.getType().name());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent event) {
        Entity entity = event.getEntity();
        Player player = getPlayerWithinRadius(entity.getLocation());

        if (player != null) {
            mainListener.handleEntityPlace(event, player, entity.getLocation().getChunk(), entity.getType().name());
        }
    }
}
