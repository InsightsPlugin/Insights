package net.frankheijden.insights.listeners;

import net.frankheijden.insights.Insights;
import net.frankheijden.insights.events.PlayerEntityPlaceEvent;
import net.frankheijden.insights.managers.NMSManager;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class Pre1_13Listeners implements Listener {
    private final MainListener mainListener;

    private final boolean vehicleCancellable;
    private final Map<String, Material> vehicleMap;
    private final Map<String, Material> boatMap;

    public Pre1_13Listeners(MainListener mainListener) {
        this.mainListener = mainListener;
        this.vehicleCancellable = VehicleCreateEvent.class.isAssignableFrom(Cancellable.class);

        this.vehicleMap = new HashMap<>();
        this.vehicleMap.put("MINECART_CHEST", Material.valueOf("STORAGE_MINECART"));
        this.vehicleMap.put("MINECART_FURNACE", Material.valueOf("POWERED_MINECART"));
        this.vehicleMap.put("MINECART_COMMAND", Material.valueOf("COMMAND_MINECART"));
        this.vehicleMap.put("MINECART_HOPPER", Material.valueOf("HOPPER_MINECART"));

        this.boatMap = new HashMap<>();
        if (NMSManager.getInstance().isPost1_12()) {
            this.boatMap.put("ACACIA", Material.valueOf("BOAT_ACACIA"));
            this.boatMap.put("BIRCH", Material.valueOf("BOAT_BIRCH"));
            this.boatMap.put("DARK_OAK", Material.valueOf("BOAT_DARK_OAK"));
            this.boatMap.put("GENERIC", Material.valueOf("BOAT"));
            this.boatMap.put("JUNGLE", Material.valueOf("BOAT_JUNGLE"));
            this.boatMap.put("REDWOOD", Material.valueOf("BOAT_SPRUCE"));
        }
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

    private Material getMaterial(Vehicle vehicle) {
        String name = vehicle.getType().name();
        Material m;
        if (vehicle instanceof Boat && NMSManager.getInstance().isPost1_12()) {
            Boat boat = (Boat) vehicle;
            m = boatMap.get(boat.getWoodType().name());
        } else {
            m = vehicleMap.get(name);
        }
        if (m != null) return m;
        return Material.valueOf(name);
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
                        Material m = getMaterial(vehicle);
                        player.getInventory().addItem(new ItemStack(m, 1));
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
