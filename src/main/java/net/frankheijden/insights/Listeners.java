package net.frankheijden.insights;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.frankheijden.insights.api.events.PlayerChunkMoveEvent;
import net.frankheijden.insights.api.events.ScanCompleteEvent;
import net.frankheijden.insights.tasks.UpdateCheckerTask;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.NumberFormat;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Listeners implements Listener {
    private Insights plugin;

    Listeners(Insights plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material material = event.getBlock().getType();

        if (!isScanningEnabledInWorld(player.getWorld())) {
            return;
        }

        if (!plugin.getConfiguration().GENERAL_REGIONS_LIST.isEmpty()) {
            if (plugin.getWorldGuardUtils() != null) {
                ProtectedRegion region = plugin.getWorldGuardUtils().isInRegion(player);
                if (region != null) {
                    if (!isScanningEnabledInRegion(region.getId())) {
                        return;
                    }
                }
            }
        }

        if (plugin.getConfiguration().GENERAL_MATERIALS.keySet().contains(material)) {
            int limit = plugin.getConfiguration().GENERAL_MATERIALS.get(material);
            sendBreakMessage(player, event.getBlock().getChunk(), material, limit);
            return;
        }

        if (plugin.getUtils().isTile(event.getBlock())) {
            int limit = plugin.getConfiguration().GENERAL_LIMIT;
            if (plugin.getConfiguration().GENERAL_ALWAYS_SHOW_NOTIFICATION || limit > -1) {
                if (player.hasPermission("insights.check.realtime") && plugin.getSqLite().hasRealtimeCheckEnabled(player)) {
                    int current = event.getBlock().getLocation().getChunk().getTileEntities().length - 1;
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

    private void sendBreakMessage(Player player, Chunk chunk, Material material, int limit) {
        ChunkSnapshot chunkSnapshot = chunk.getChunkSnapshot();
        new BukkitRunnable() {
            @Override
            public void run() {
                int current = plugin.getUtils().getAmountInChunk(chunkSnapshot, material);
                if (player.hasPermission("insights.check.realtime") && plugin.getSqLite().hasRealtimeCheckEnabled(player)) {
                    double progress = ((double) current)/((double) limit);
                    if (progress > 1 || progress < 0) progress = 1;
                    plugin.getUtils().sendSpecialMessage(player, "messages.realtime_check_custom", progress, "%count%", NumberFormat.getIntegerInstance().format(current), "%material%", plugin.getUtils().capitalizeName(material.name().toLowerCase()), "%limit%", NumberFormat.getIntegerInstance().format(limit));
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Material material = event.getBlock().getType();

        if (!canPlace(player, material) && !player.hasPermission("insights.regions.bypass." + material.name())) {
            plugin.getUtils().sendMessage(player, "messages.region_disallowed_block");
            event.setCancelled(true);
            return;
        }

        if (!isScanningEnabledInWorld(player.getWorld())) {
            return;
        }

        if (!plugin.getConfiguration().GENERAL_REGIONS_LIST.isEmpty()) {
            if (plugin.getWorldGuardUtils() != null) {
                ProtectedRegion region = plugin.getWorldGuardUtils().isInRegion(player);
                if (region != null) {
                    if (!isScanningEnabledInRegion(region.getId())) {
                        return;
                    }
                }
            }
        }

        if (plugin.getConfiguration().GENERAL_MATERIALS.keySet().contains(material)) {
            int limit = plugin.getConfiguration().GENERAL_MATERIALS.get(material);
            handleBlockPlace(player, event.getBlock(), material, event.getItemInHand(), limit);
            return;
        }

        if (plugin.getUtils().isTile(event.getBlockPlaced())) {
            int current = event.getBlock().getLocation().getChunk().getTileEntities().length + 1;
            int limit = plugin.getConfiguration().GENERAL_LIMIT;
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

    private boolean canPlace(Player player, Material material) {
        if (plugin.getWorldGuardUtils() != null) {
            ProtectedRegion region = plugin.getWorldGuardUtils().isInRegionBlocks(player);
            if (region != null) {
                Boolean whitelist = plugin.getConfiguration().GENERAL_REGION_BLOCKS_WHITELIST.get(region.getId());
                if (whitelist != null) {
                    if (whitelist) {
                        return plugin.getConfiguration().GENERAL_REGION_BLOCKS_LIST.get(region.getId()).contains(material);
                    } else {
                        return !plugin.getConfiguration().GENERAL_REGION_BLOCKS_LIST.get(region.getId()).contains(material);
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

    private void handleBlockPlace(Player player, Block block, Material material, ItemStack itemInHand, int limit) {
        ChunkSnapshot chunkSnapshot = block.getChunk().getChunkSnapshot();
        new BukkitRunnable() {
            @Override
            public void run() {
                int current = plugin.getUtils().getAmountInChunk(chunkSnapshot, material);
                if (current > limit) {
                    if (!player.hasPermission("insights.bypass." + material.name())) {
                        plugin.getUtils().sendMessage(player, "messages.limit_reached_custom", "%limit%", NumberFormat.getIntegerInstance().format(limit), "%material%", plugin.getUtils().capitalizeName(material.name().toLowerCase()));
                        if (player.getGameMode() != GameMode.CREATIVE) {
                            ItemStack itemStack = new ItemStack(itemInHand);
                            itemStack.setAmount(1);
                            player.getInventory().addItem(itemStack);
                        }
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                block.setType(Material.AIR);
                            }
                        }.runTask(plugin);
                    }
                }

                if (player.hasPermission("insights.check.realtime") && plugin.getSqLite().hasRealtimeCheckEnabled(player)) {
                    double progress = ((double) current)/((double) limit);
                    if (progress > 1 || progress < 0) progress = 1;
                    plugin.getUtils().sendSpecialMessage(player, "messages.realtime_check_custom", progress, "%count%", NumberFormat.getIntegerInstance().format(current), "%material%", plugin.getUtils().capitalizeName(material.name().toLowerCase()), "%limit%", NumberFormat.getIntegerInstance().format(limit));
                }
            }
        }.runTaskAsynchronously(plugin);
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
                completableFuture.whenCompleteAsync((e, error) -> {
                    Map.Entry<String, Integer> entry = e.getCounts().firstEntry();
                    if (material != null) {
                        if (plugin.getConfiguration().GENERAL_MATERIALS.containsKey(material)) {
                            int limit = plugin.getConfiguration().GENERAL_MATERIALS.get(material);
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
