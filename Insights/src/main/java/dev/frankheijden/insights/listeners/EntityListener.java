package dev.frankheijden.insights.listeners;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.events.EntityRemoveFromWorldEvent;
import dev.frankheijden.insights.api.listeners.InsightsListener;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.projectiles.ProjectileSource;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class EntityListener extends InsightsListener {

    protected static final Set<EntityType> LIMITED_ENTITIES = EnumSet.of(
            EntityType.ARMOR_STAND,
            EntityType.ENDER_CRYSTAL,
            EntityType.ITEM_FRAME,
            EntityType.PAINTING
    );

    private final Set<UUID> removedEntities;

    public EntityListener(InsightsPlugin plugin) {
        super(plugin);
        this.removedEntities = new HashSet<>();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityBreakDoor(EntityBreakDoorEvent event) {
        // A door accounts for 2 blocks
        handleModification(event.getBlock(), -2);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        Block block = event.getBlock();
        handleModification(block.getLocation(), block.getType(), event.getTo(), 1);
    }

    /**
     * Handles the HangingPlaceEvent for Item Frames and Paintings.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event) {
        if (handleEntityPlace(event.getPlayer(), event.getEntity())) {
            event.setCancelled(true);
        }
    }

    /**
     * Handles the EntityPlaceEvent for Armor Stands and End Crystals.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityPlace(EntityPlaceEvent event) {
        if (handleEntityPlace(event.getPlayer(), event.getEntity())) {
            event.setCancelled(true);
        }
    }

    /**
     * Handles the HangingBreakEvent for Item Frames and Paintings.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent event) {
        Entity entity = event.getEntity();
        EntityType entityType = entity.getType();
        if (!LIMITED_ENTITIES.contains(entityType)) return;
        removedEntities.add(entity.getUniqueId());

        Location location = entity.getLocation();

        int delta = 1;
        if (event instanceof HangingBreakByEntityEvent) {
            Entity remover = ((HangingBreakByEntityEvent) event).getRemover();
            if (remover instanceof Player) {
                handleRemoval((Player) remover, location, entityType, delta);
                return;
            }
        }

        // Update the cache if it was not broken by a player (but instead by e.g. physics)
        handleModification(location, entityType, -delta);
    }

    /**
     * Handles the EntityDeathEvent for Armor Stands.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        handleEntityRemoval(event.getEntity());
    }

    /**
     * Handles the EntityExplodeEvent for End Crystals.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityExplosion(EntityExplodeEvent event) {
        handleEntityRemoval(event.getEntity());
        for (Block block : event.blockList()) {
            handleModification(block, -1);
        }
    }

    /**
     * Handles the EntityRemoveFromWorldEvent as "catch-all" for entity removals.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityRemoveFromWorld(EntityRemoveFromWorldEvent event) {
        if (!LIMITED_ENTITIES.contains(event.getEntityType())) return;
        if (removedEntities.remove(event.getEntity().getUniqueId())) return;
        handleEntityRemoval(event.getEntity());
    }

    protected boolean handleEntityPlace(Player player, Entity entity) {
        EntityType entityType = entity.getType();
        if (!LIMITED_ENTITIES.contains(entityType)) return false;

        Location location = entity.getLocation();
        int delta = 1;

        if (handleAddition(player, location, entityType, delta, false)) {
            return true;
        }

        handleModification(location, entityType, delta);
        return false;
    }

    protected void handleEntityRemoval(Entity entity) {
        EntityType entityType = entity.getType();
        if (!LIMITED_ENTITIES.contains(entityType)) return;
        removedEntities.add(entity.getUniqueId());

        Location location = entity.getLocation();
        int delta = 1;

        Optional<Player> player = getPlayerKiller(entity);
        if (player.isPresent()) {
            handleRemoval(player.get(), location, entityType, delta);
            return;
        }

        // Update the cache if it was not removed by a player
        handleModification(location, entityType, -delta);
    }

    /**
     * Tries to figure out the player who killed the given entity.
     */
    protected Optional<Player> getPlayerKiller(Entity entity) {
        EntityDamageEvent event = entity.getLastDamageCause();
        if (event instanceof EntityDamageByEntityEvent) {
            return getPlayer(((EntityDamageByEntityEvent) event).getDamager());
        }
        return Optional.empty();
    }

    /**
     * Tries to figure out the player from a given "damager" entity.
     */
    protected Optional<Player> getPlayer(Entity damager) {
        if (damager instanceof Player) {
            return Optional.of((Player) damager);
        } else if (damager instanceof Projectile) {
            ProjectileSource source = ((Projectile) damager).getShooter();
            if (source instanceof Player) {
                return Optional.of((Player) source);
            }
        } else if (damager instanceof Tameable) {
            AnimalTamer tamer = ((Tameable) damager).getOwner();
            if (tamer instanceof Player) {
                return Optional.of((Player) tamer);
            }
        }
        return Optional.empty();
    }
}
