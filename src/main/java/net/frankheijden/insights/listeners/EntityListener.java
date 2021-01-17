package net.frankheijden.insights.listeners;

import io.papermc.lib.PaperLib;
import net.frankheijden.insights.Insights;
import net.frankheijden.insights.events.EntityRemoveFromWorldEvent;
import net.frankheijden.insights.events.PlayerEntityDestroyEvent;
import net.frankheijden.insights.events.PlayerEntityPlaceEvent;
import net.frankheijden.insights.managers.CacheManager;
import net.frankheijden.insights.managers.NMSManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class EntityListener implements Listener {

    private static final CacheManager cacheManager = CacheManager.getInstance();
    private final MainListener listener;
    private final Set<UUID> removedEntities = new HashSet<>();
    private PaperEntityListener paperEntityListener = null;

    public EntityListener(MainListener listener) {
        this.listener = listener;

        if (PaperLib.isPaper()) {
            paperEntityListener = new PaperEntityListener();
        } else {
            Bukkit.getScheduler().runTaskTimerAsynchronously(Insights.getInstance(), new EntityRemovalChecker(), 0, 20);
        }
    }

    public PaperEntityListener getPaperEntityListener() {
        return paperEntityListener;
    }

    public void onRemoveEntity(UUID uuid) {
        removedEntities.add(uuid);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getEntity();

        handleEntityPlaceEvent(event, player, entity);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        Entity entity = event.getEntity();

        // >= 1.13 EntityPlaceEvent is called for Armor Stands.
        if (entity.getType() == EntityType.ARMOR_STAND && NMSManager.getInstance().isPost(13)) return;

        Player player = listener.getInteractListener().getPlayerWithinRadius(entity.getLocation());

        boolean ignore = Insights.getInstance().getConfiguration().GENERAL_IGNORE_CUSTOM_SPAWN && event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM;
        if (player != null && !ignore) {
            handleEntityPlaceEvent(event, player, entity);
        } else {
            cacheManager.newCacheLocation(entity.getLocation()).updateCache(entity.getType().name(), 1);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        Entity remover = event.getRemover();
        if (!(remover instanceof Player)) return;

        handleEntityDestroyEvent(event, (Player) remover, event.getEntity());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        Entity remover = event.getAttacker();
        if (!(remover instanceof Player)) return;

        handleEntityDestroyEvent(event, (Player) remover, event.getVehicle());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        EntityDamageEvent entityDamageEvent = entity.getLastDamageCause();
        if (entityDamageEvent instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) entityDamageEvent;
            Entity damager = entityDamageByEntityEvent.getDamager();

            if (damager instanceof Player) {
                // On some minecraft versions this statement is false
                if (event instanceof Cancellable) {
                    handleEntityDestroyEvent(event, (Player) damager, entity);
                    if (!event.isCancelled()) onRemoveEntity(entity.getUniqueId());
                } else {
                    handleEntityDestroyEvent(entityDamageByEntityEvent, (Player) damager, entity);
                    if (!entityDamageByEntityEvent.isCancelled()) onRemoveEntity(entity.getUniqueId());
                }
                return;
            }
        }
        onRemoveEntity(entity.getUniqueId());
        handleEntityChange(entity, false);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityRemoveFromWorld(EntityRemoveFromWorldEvent event) {
        Entity entity = event.getEntity();
        if (removedEntities.remove(entity.getUniqueId())) return;
        handleEntityChange(event.getEntity(), false);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Some fallback method for armorstands <= MC 1.10.2
        // because Armorstand destroy isn't called on EntityDeath
        if (PaperLib.getMinecraftVersion() >= 11) return;
        Entity damager = event.getDamager();
        Entity entity = event.getEntity();
        if (!(damager instanceof Player)) return;
        if (!(entity instanceof ArmorStand)) return;
        handleEntityDestroyEvent(event, (Player) damager, entity);
    }

    public static void handleEntityChange(Entity entity, boolean added) {
        cacheManager.newCacheLocation(entity.getLocation()).updateCache(entity.getType().name(), added ? 1 : -1);
    }

    public static void handleEntityPlaceEvent(Cancellable cancellable, Player player, Entity entity) {
        PlayerEntityPlaceEvent entityPlaceEvent = new PlayerEntityPlaceEvent(player, entity);
        Bukkit.getPluginManager().callEvent(entityPlaceEvent);
        cancellable.setCancelled(entityPlaceEvent.isCancelled());
    }

    public static void handleEntityDestroyEvent(Cancellable cancellable, Player player, Entity entity) {
        PlayerEntityDestroyEvent entityDestroyEvent = new PlayerEntityDestroyEvent(player, entity);
        Bukkit.getPluginManager().callEvent(entityDestroyEvent);
        cancellable.setCancelled(entityDestroyEvent.isCancelled());
    }

    private static final class EntityRemovalChecker implements Runnable {

        private Map<UUID, Entity> currentEntities = new HashMap<>();

        private final Object LOCK = new Object();
        private final AtomicBoolean busy = new AtomicBoolean(false);

        @Override
        public void run() {
            if (busy.getAndSet(true)) return;

            Map<UUID, Entity> entities = new HashMap<>();
            for (World w : Bukkit.getWorlds()) {
                for (Entity e : w.getEntities()) {
                    entities.put(e.getUniqueId(), e);
                }
            }

            currentEntities.keySet().removeAll(entities.keySet());

            Bukkit.getScheduler().runTask(Insights.getInstance(), () -> {
                for (Entity entity : currentEntities.values()) {
                    Bukkit.getPluginManager().callEvent(new EntityRemoveFromWorldEvent(entity));
                }

                synchronized (LOCK) {
                    LOCK.notify();
                }
            });

            synchronized (LOCK) {
                try {
                    LOCK.wait();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }

            currentEntities.clear();
            currentEntities = entities;
            busy.set(false);
        }
    }
}
