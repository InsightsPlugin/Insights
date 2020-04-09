package net.frankheijden.insights.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.*;

public class InteractListener implements Listener {
    private Map<Location, Player> interactLocations;
    private Map<Location, Long> interactTimes;

    public InteractListener() {
        this.interactLocations = new HashMap<>();
        this.interactTimes = new HashMap<>();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;

        Location loc = event.getClickedBlock().getLocation();
        interactLocations.put(loc, event.getPlayer());
        interactTimes.put(loc, System.currentTimeMillis());
    }

    public Player getPlayerWithinRadius(Location location) {
        List<Location> locationsToRemove = new ArrayList<>();
        for (Location loc : interactLocations.keySet()) {
            if (System.currentTimeMillis() - interactTimes.get(loc) > 500) {
                locationsToRemove.add(loc);
                continue;
            }

            if (!loc.getWorld().equals(location.getWorld())) continue;

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
}
