package dev.frankheijden.insights.listeners;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.annotations.AllowDisabling;
import dev.frankheijden.insights.api.annotations.AllowPriorityOverride;
import dev.frankheijden.insights.api.events.EntityRemoveFromWorldEvent;
import dev.frankheijden.insights.api.listeners.InsightsListener;
import dev.frankheijden.insights.api.objects.wrappers.ScanObject;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
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
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class EntityListener extends InsightsListener {

    protected static final Set<EntityType> LIMITED_ENTITIES = EnumSet.of(
            EntityType.ARMOR_STAND,
            EntityType.END_CRYSTAL,
            EntityType.ITEM_FRAME,
            EntityType.GLOW_ITEM_FRAME,
            EntityType.PAINTING
    );

    /**
     * Spawn reasons of mobs which are built out of blocks. Derived at runtime so that reasons
     * added by later Minecraft versions (e.g. BUILD_COPPERGOLEM) are picked up as well.
     */
    protected static final Set<CreatureSpawnEvent.SpawnReason> BUILD_SPAWN_REASONS = Arrays
            .stream(CreatureSpawnEvent.SpawnReason.values())
            .filter(reason -> reason.name().startsWith("BUILD_"))
            .collect(Collectors.toCollection(() -> EnumSet.noneOf(CreatureSpawnEvent.SpawnReason.class)));

    /**
     * The widest build pattern (wither, iron golem) reaches two blocks away from the position the
     * mob is spawned at, in any orientation the pattern may have been matched in.
     */
    protected static final int BUILD_PATTERN_RADIUS = 2;

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
    @AllowPriorityOverride
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event) {
        if (handleEntityPlace(event.getPlayer(), event.getEntity())) {
            event.setCancelled(true);
        }
    }

    /**
     * Monitors the HangingPlaceEvent for Item Frames and Paintings.
     * This event does not limit, it only monitors results from the event.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHangingPlaceMonitor(HangingPlaceEvent event) {
        evaluateEntityPlace(event.getPlayer(), event.getEntity());
    }

    /**
     * Handles the EntityPlaceEvent for Armor Stands and End Crystals.
     */
    @AllowPriorityOverride
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityPlace(EntityPlaceEvent event) {
        if (handleEntityPlace(event.getPlayer(), event.getEntity())) {
            event.setCancelled(true);
        }
    }

    /**
     * Monitors the EntityPlaceEvent for Armor Stands and End Crystals.
     * This event does not limit, it only monitors results from the event.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityPlaceMonitor(EntityPlaceEvent event) {
        evaluateEntityPlace(event.getPlayer(), event.getEntity());
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
                handleRemoval((Player) remover, location, ScanObject.of(entityType), delta);
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
        handleEntityRemoval(event.getEntity(), true);
    }

    /**
     * Handles the EntityExplodeEvent for End Crystals.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityExplosion(EntityExplodeEvent event) {
        handleEntityRemoval(event.getEntity(), true);
        for (Block block : event.blockList()) {
            handleModification(block, -1);
        }
    }

    /**
     * Handles the EntityRemoveFromWorldEvent as "catch-all" for entity removals.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityRemoveFromWorld(EntityRemoveFromWorldEvent event) {
        handleEntityRemoveFromWorld(event.getEntity());
    }

    /**
     * Handles the CreatureSpawnEvent for mobs which are built out of blocks.
     *
     * <p>The blocks making up a wither or golem are consumed with a direct block set, which fires
     * no block event at all, so the cache is never told about them. The same goes for the chest a
     * copper golem leaves behind. Snapshot the area around the spawn and reconcile whatever
     * actually changed once those changes have been applied.
     */
    @AllowDisabling
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!BUILD_SPAWN_REASONS.contains(event.getSpawnReason())) return;
        reconcileBuildPattern(event.getLocation());
    }

    /**
     * Reconciles the cache with the blocks a build pattern consumed or left behind.
     */
    private void reconcileBuildPattern(Location location) {
        World world = location.getWorld();
        int minY = Math.max(world.getMinHeight(), location.getBlockY() - BUILD_PATTERN_RADIUS);
        int maxY = Math.min(world.getMaxHeight() - 1, location.getBlockY() + BUILD_PATTERN_RADIUS);

        Map<Block, Material> snapshot = new HashMap<>();
        for (int x = location.getBlockX() - BUILD_PATTERN_RADIUS; x <= location.getBlockX() + BUILD_PATTERN_RADIUS; x++) {
            for (int z = location.getBlockZ() - BUILD_PATTERN_RADIUS; z <= location.getBlockZ() + BUILD_PATTERN_RADIUS; z++) {
                // Never load a chunk for this, an unloaded chunk holds no cache to correct anyway.
                if (!world.isChunkLoaded(x >> 4, z >> 4)) continue;

                for (int y = minY; y <= maxY; y++) {
                    Block block = world.getBlockAt(x, y, z);
                    snapshot.put(block, block.getType());
                }
            }
        }

        if (snapshot.isEmpty()) return;

        plugin.getServer().getRegionScheduler().runDelayed(plugin, location, scheduledTask -> {
            snapshot.forEach((block, material) -> {
                Material current = block.getType();
                if (current != material) {
                    handleModification(block.getLocation(), material, current, 1);
                }
            });
        }, 1L); // The pattern is cleared right after the spawn event, within the same tick
    }

    protected void handleEntityRemoveFromWorld(Entity entity) {
        if (!entity.isDead() || removedEntities.remove(entity.getUniqueId())) return;
        handleEntityRemoval(entity, false);
    }

    protected boolean handleEntityPlace(Player player, Entity entity) {
        EntityType entityType = entity.getType();
        if (!LIMITED_ENTITIES.contains(entityType)) return false;
        return handleAddition(player, entity.getLocation(), ScanObject.of(entityType), 1, false);
    }

    protected void evaluateEntityPlace(Player player, Entity entity) {
        EntityType entityType = entity.getType();
        if (!LIMITED_ENTITIES.contains(entityType)) return;

        Location location = entity.getLocation();
        int delta = 1;

        evaluateAddition(player, location, ScanObject.of(entityType), delta);
        handleModification(location, entityType, delta);
    }

    protected void handleEntityRemoval(Entity entity, boolean isPlayer) {
        EntityType entityType = entity.getType();
        if (!LIMITED_ENTITIES.contains(entityType)) return;

        Location location = entity.getLocation();
        int delta = 1;

        if (isPlayer) {
            removedEntities.add(entity.getUniqueId());
            Optional<Player> player = getPlayerKiller(entity);
            if (player.isPresent()) {
                handleRemoval(player.get(), location, ScanObject.of(entityType), delta);
                return;
            }
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
