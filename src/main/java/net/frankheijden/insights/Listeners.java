package net.frankheijden.insights;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.frankheijden.insights.api.events.PlayerChunkMoveEvent;
import net.frankheijden.insights.api.events.ScanCompleteEvent;
import net.frankheijden.insights.tasks.UpdateCheckerTask;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Listeners implements Listener {
    private Insights plugin;

    private List<Location> blockLocations;

    Listeners(Insights plugin) {
        this.plugin = plugin;
        this.blockLocations = new ArrayList<>();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        String name = event.getBlock().getType().name();

        int limit = getLimit(player, name);
        if (limit > -1) {
            sendBreakMessage(player, event.getBlock().getChunk(), name, limit);
        } else if (plugin.getUtils().isTile(event.getBlock())) {
            limit = plugin.getConfiguration().GENERAL_LIMIT;
            if (plugin.getConfiguration().GENERAL_ALWAYS_SHOW_NOTIFICATION || limit > -1) {
                if (player.hasPermission("insights.check.realtime") && plugin.getSqLite().hasRealtimeCheckEnabled(player)) {
                    int current = event.getBlock().getLocation().getChunk().getTileEntities().length - 1;
                    double progress = ((double) current)/((double) limit);
                    if (progress > 1 || progress < 0) progress = 1;

                    if (limit > -1) {
                        plugin.getUtils().sendSpecialMessage(player, "messages.realtime_check", progress,
                                "%tile_count%", NumberFormat.getIntegerInstance().format(current),
                                "%limit%", NumberFormat.getIntegerInstance().format(limit));
                    } else {
                        plugin.getUtils().sendSpecialMessage(player, "messages.realtime_check_no_limit", progress,
                                "%tile_count%", NumberFormat.getIntegerInstance().format(current));
                    }
                }
            }
        }
    }

    private void sendBreakMessage(Player player, Chunk chunk, String materialString, int limit) {
        ChunkSnapshot chunkSnapshot = chunk.getChunkSnapshot();
        new BukkitRunnable() {
            @Override
            public void run() {
                int current = plugin.getUtils().getAmountInChunk(chunkSnapshot, materialString) - 1;
                sendMessage(player, materialString, current, limit);
            }
        }.runTaskAsynchronously(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        Entity remover = event.getRemover();
        if (!(remover instanceof Player)) return;
        handleEntityDestroy((Player) remover, event.getEntity());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        Entity remover = event.getAttacker();
        if (!(remover instanceof Player)) return;
        handleEntityDestroy((Player) remover, event.getVehicle());
    }

    public void handleEntityDestroy(Player player, Entity entity) {
        String name = entity.getType().name();

        int limit = getLimit(player, name);
        if (limit < 0) return;
        int current = getEntityCount(entity.getChunk(), name) - 1;

        sendMessage(player, name, current, limit);
    }

    private void sendMessage(Player player, String name, int current, int limit) {
        if (player.hasPermission("insights.check.realtime") && plugin.getSqLite().hasRealtimeCheckEnabled(player)) {
            double progress = ((double) current)/((double) limit);
            if (progress > 1 || progress < 0) progress = 1;
            plugin.getUtils().sendSpecialMessage(player, "messages.realtime_check_custom", progress,
                    "%count%", NumberFormat.getIntegerInstance().format(current),
                    "%material%", plugin.getUtils().capitalizeName(name.toLowerCase()),
                    "%limit%", NumberFormat.getIntegerInstance().format(limit));
        }
    }

    private int getLimit(Player player, String name) {
        if (!isScanningEnabledInWorld(player.getWorld())) {
            return -1;
        }

        if (!plugin.getConfiguration().GENERAL_REGIONS_LIST.isEmpty()) {
            if (plugin.getWorldGuardUtils() != null) {
                ProtectedRegion region = plugin.getWorldGuardUtils().isInRegion(player);
                if (region != null) {
                    if (!isScanningEnabledInRegion(region.getId())) {
                        return -1;
                    }
                }
            }
        }

        for (String permission : plugin.getConfiguration().GENERAL_GROUPS.keySet()) {
            if (player.hasPermission(permission)) {
                if (plugin.getConfiguration().GENERAL_GROUPS.get(permission).containsKey(name)) {
                    return plugin.getConfiguration().GENERAL_GROUPS.get(permission).get(name);
                }
            }
        }

        if (plugin.getConfiguration().GENERAL_MATERIALS.containsKey(name)) {
            return plugin.getConfiguration().GENERAL_MATERIALS.get(name);
        }
        return -1;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityPlace(EntityPlaceEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;
        handleEntityPlace(event, player, event.getEntity().getChunk(), event.getEntityType().name());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getEntity();
        String name = entity.getType().name();
        handleEntityPlace(event, player, entity.getChunk(), name);
    }

    public void handleEntityPlace(Cancellable cancellable, Player player, Chunk chunk, String name) {
        if (!canPlace(player, name) && !player.hasPermission("insights.regions.bypass." + name)) {
            plugin.getUtils().sendMessage(player, "messages.region_disallowed_block");
            cancellable.setCancelled(true);
            return;
        }

        int limit = getLimit(player, name);
        if (limit < 0) return;
        int current = getEntityCount(chunk, name) + 1;

        if (current > limit && !player.hasPermission("insights.bypass." + name)) {
            cancellable.setCancelled(true);
            plugin.getUtils().sendMessage(player, "messages.limit_reached_custom",
                    "%limit%", NumberFormat.getIntegerInstance().format(limit),
                    "%material%", plugin.getUtils().capitalizeName(name.toLowerCase()));
            return;
        }

        sendMessage(player, name, current, limit);
    }

    private int getEntityCount(Chunk chunk, String entityType) {
        int count = 0;
        for (Entity entity : chunk.getEntities()) {
            if (entity.getType().name().equals(entityType)) count++;
        }
        return count;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (plugin.getHookManager().shouldCancel(event.getBlock())) return;

        Player player = event.getPlayer();
        String name = event.getBlock().getType().name();

        if (isNextToForbiddenLocation(event.getBlock().getLocation())) {
            event.setCancelled(true);
            plugin.log(Insights.LogType.WARNING, "Player " + player.getPlayerListName() + " placed block '" + name + "' too fast nearby a limited block.");
            return;
        }

        if (!canPlace(player, name) && !player.hasPermission("insights.regions.bypass." + name)) {
            plugin.getUtils().sendMessage(player, "messages.region_disallowed_block");
            event.setCancelled(true);
            return;
        }

        int limit = getLimit(player, name);
        if (limit > -1) {
            handleBlockPlace(event, player, event.getBlock(), name, event.getItemInHand(), limit);
        } else if (plugin.getUtils().isTile(event.getBlockPlaced())) {
            int current = event.getBlock().getLocation().getChunk().getTileEntities().length + 1;
            limit = plugin.getConfiguration().GENERAL_LIMIT;
            if (limit > -1 && current >= limit) {
                if (!player.hasPermission("insights.bypass")) {
                    event.setCancelled(true);
                    plugin.getUtils().sendMessage(player, "messages.limit_reached", "%limit%", NumberFormat.getIntegerInstance().format(limit));
                }
            }

            if (plugin.getConfiguration().GENERAL_ALWAYS_SHOW_NOTIFICATION || limit > -1) {
                if (player.hasPermission("insights.check.realtime") && plugin.getSqLite().hasRealtimeCheckEnabled(player)) {
                    double progress = ((double) current)/((double) limit);
                    if (progress > 1 || progress < 0) progress = 1;

                    if (limit > -1) {
                        plugin.getUtils().sendSpecialMessage(player, "messages.realtime_check", progress, "%tile_count%", NumberFormat.getIntegerInstance().format(current), "%limit%", NumberFormat.getIntegerInstance().format(limit));
                    } else {
                        plugin.getUtils().sendSpecialMessage(player, "messages.realtime_check_no_limit", progress, "%tile_count%", NumberFormat.getIntegerInstance().format(current));
                    }
                }
            }
        }
    }

    private boolean isNextToForbiddenLocation(Location location) {
        for (Location loc : blockLocations) {
            if (isEqual(loc, location, -1, 0, 0)
                    || isEqual(loc, location, 1, 0, 0)
                    || isEqual(loc, location, 0, -1, 0)
                    || isEqual(loc, location, 0, 1, 0)
                    || isEqual(loc, location, 0, 0, -1)
                    || isEqual(loc, location, 0, 0, 1)) return true;
        }
        return false;
    }

    private boolean isEqual(Location loc1, Location loc2, int x, int y, int z) {
        return loc1.clone().add(x, y, z).equals(loc2);
    }

    private boolean canPlace(Player player, String itemString) {
        if (plugin.getWorldGuardUtils() != null) {
            ProtectedRegion region = plugin.getWorldGuardUtils().isInRegionBlocks(player);
            if (region != null) {
                Boolean whitelist = plugin.getConfiguration().GENERAL_REGION_BLOCKS_WHITELIST.get(region.getId());
                if (whitelist != null) {
                    if (whitelist) {
                        return plugin.getConfiguration().GENERAL_REGION_BLOCKS_LIST.get(region.getId()).contains(itemString);
                    } else {
                        return !plugin.getConfiguration().GENERAL_REGION_BLOCKS_LIST.get(region.getId()).contains(itemString);
                    }
                }
            }
        }
        return true;
    }

    private boolean isScanningEnabledInWorld(World world) {
        if (plugin.getConfiguration().GENERAL_WORLDS_WHITELIST) {
            if (!plugin.getConfiguration().GENERAL_WORLDS_LIST.contains(world.getName())) {
                return false;
            }
        } else {
            if (plugin.getConfiguration().GENERAL_WORLDS_LIST.contains(world.getName())) {
                return false;
            }
        }
        return true;
    }

    private boolean isScanningEnabledInRegion(String region) {
        if (plugin.getConfiguration().GENERAL_REGIONS_WHITELIST) {
            if (!plugin.getConfiguration().GENERAL_REGIONS_LIST.contains(region)) {
                return false;
            }
        } else {
            if (plugin.getConfiguration().GENERAL_REGIONS_LIST.contains(region)) {
                return false;
            }
        }
        return true;
    }

    private void handleBlockPlace(Cancellable event, Player player, Block block, String materialString, ItemStack itemInHand, int limit) {
        ChunkSnapshot chunkSnapshot = block.getChunk().getChunkSnapshot();

        boolean async = plugin.getConfiguration().GENERAL_SCAN_ASYNC;
        if (async) {
            ItemStack itemStack = new ItemStack(itemInHand);
            itemStack.setAmount(1);

            if (!player.hasPermission("insights.bypass." + materialString)) {
                blockLocations.add(block.getLocation());
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    handleBlockPlace(event, player, block, chunkSnapshot, materialString, itemStack, async, limit);
                }
            }.runTaskAsynchronously(plugin);
        } else {
            handleBlockPlace(event, player, block, chunkSnapshot, materialString, null, async, limit);
        }
    }

    private void handleBlockPlace(Cancellable event, Player player, Block block, ChunkSnapshot chunkSnapshot, String materialString, ItemStack itemStack, boolean async, int limit) {
        int current = plugin.getUtils().getAmountInChunk(chunkSnapshot, materialString);
        if (current > limit) {
            if (!player.hasPermission("insights.bypass." + materialString)) {
                plugin.getUtils().sendMessage(player, "messages.limit_reached_custom", "%limit%", NumberFormat.getIntegerInstance().format(limit), "%material%", plugin.getUtils().capitalizeName(materialString.toLowerCase()));
                if (async) {
                    if (player.getGameMode() != GameMode.CREATIVE) {
                        player.getInventory().addItem(itemStack);
                    }
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            block.setType(Material.AIR);
                            blockLocations.remove(block.getLocation());
                        }
                    }.runTask(plugin);
                    return;
                } else {
                    event.setCancelled(true);
                }
            }
        } else {
            sendMessage(player, materialString, current, limit);
        }
        blockLocations.remove(block.getLocation());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (plugin.getBossBarUtils() != null && plugin.getBossBarUtils().scanBossBarPlayers.containsKey(uuid)) {
            plugin.getBossBarUtils().scanBossBarPlayers.get(uuid).removeAll();
            plugin.getBossBarUtils().scanBossBarPlayers.get(uuid).addPlayer(player);
        }

        if (plugin.getConfiguration().GENERAL_UPDATES_CHECK) {
            if (player.hasPermission("insights.notification.update")) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, new UpdateCheckerTask(plugin, player));
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Chunk fromChunk = event.getFrom().getChunk();
        Chunk toChunk = event.getTo().getChunk();
        if (fromChunk != toChunk) {
            PlayerChunkMoveEvent chunkEnterEvent = new PlayerChunkMoveEvent(event.getPlayer(), fromChunk, toChunk);
            Bukkit.getPluginManager().callEvent(chunkEnterEvent);
            if (chunkEnterEvent.isCancelled()) {
                event.setTo(event.getFrom());
            }
        }
    }

    @EventHandler
    public void onPlayerChunkMove(PlayerChunkMoveEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        String string = plugin.getSqLite().getAutoscan(player);
        if (string != null) {
            Material material = Material.getMaterial(string);
            EntityType entityType = plugin.getUtils().getEntityType(string);

            CompletableFuture<ScanCompleteEvent> completableFuture = null;
            if (material != null) {
                completableFuture = plugin.getInsightsAPI().scanSingleChunk(event.getToChunk(), material, false);
            } else if (entityType != null) {
                completableFuture = plugin.getInsightsAPI().scanSingleChunk(event.getToChunk(), entityType, false);
            }

            if (completableFuture != null) {
                completableFuture.whenCompleteAsync((ev, error) -> {
                    Map.Entry<String, Integer> entry = ev.getScanResult().getCounts().firstEntry();
                    if (material != null) {
                        if (plugin.getConfiguration().GENERAL_MATERIALS.containsKey(material.name())) {
                            int limit = plugin.getConfiguration().GENERAL_MATERIALS.get(material.name());
                            double progress = ((double) entry.getValue())/((double) limit);
                            if (progress > 1 || progress < 0) progress = 1;
                            plugin.getUtils().sendSpecialMessage(player, "messages.autoscan.message_limit", progress,
                                    "%key%", plugin.getUtils().capitalizeName(entry.getKey()),
                                    "%count%", NumberFormat.getInstance().format(entry.getValue()),
                                    "%limit%", NumberFormat.getInstance().format(limit));
                            return;
                        }
                    }

                    plugin.getUtils().sendSpecialMessage(player, "messages.autoscan.message", 1.0,
                            "%key%", plugin.getUtils().capitalizeName(entry.getKey()),
                            "%count%", NumberFormat.getInstance().format(entry.getValue()));
                });
            }
        }
    }
}
