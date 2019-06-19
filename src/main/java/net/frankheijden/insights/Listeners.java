package net.frankheijden.insights;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.frankheijden.insights.tasks.UpdateCheckerTask;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.NumberFormat;
import java.util.UUID;

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
        new BukkitRunnable() {
            @Override
            public void run() {
                int current = plugin.getUtils().getAmountInChunk(chunk, material);
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
        new BukkitRunnable() {
            @Override
            public void run() {
                int current = plugin.getUtils().getAmountInChunk(block.getChunk(), material);
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

        if (plugin.getConfiguration().GENERAL_CHECK_UPDATES) {
            if (player.hasPermission("insights.notification.update")) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, new UpdateCheckerTask(plugin, player));
            }
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        plugin.getChunkSnapshots().remove(event.getChunk().getX() + "_" + event.getChunk().getZ());
    }
}
