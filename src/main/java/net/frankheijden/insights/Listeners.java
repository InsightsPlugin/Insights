package net.frankheijden.insights;

import net.frankheijden.insights.tasks.UpdateCheckerTask;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
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

        if (!plugin.getConfiguration().GENERAL_WORLDS.contains(player.getWorld().getName())) {
            return;
        }

        Material material = event.getBlock().getType();
        if (plugin.getConfiguration().GENERAL_MATERIALS.keySet().contains(material)) {
            int limit = plugin.getConfiguration().GENERAL_MATERIALS.get(material);
            new BukkitRunnable() {
                @Override
                public void run() {
                    int current = plugin.getUtils().getAmountInChunk(event.getBlock().getChunk(), material);
                    if (player.hasPermission("insights.check.realtime") && plugin.getSqLite().hasRealtimeCheckEnabled(player)) {
                        double progress = ((double) current)/((double) limit);
                        if (progress > 1 || progress < 0) progress = 1;
                        plugin.getUtils().sendSpecialMessage(player, "messages.realtime_check_custom", progress, "%count%", NumberFormat.getIntegerInstance().format(current), "%material%", plugin.getUtils().capitalizeName(material.name().toLowerCase()), "%limit%", NumberFormat.getIntegerInstance().format(limit));
                    }
                }
            }.runTaskAsynchronously(plugin);
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

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        if (!plugin.getConfiguration().GENERAL_WORLDS.contains(player.getWorld().getName())) {
            return;
        }

        Material material = event.getBlock().getType();
        if (plugin.getConfiguration().GENERAL_MATERIALS.keySet().contains(material)) {
            int limit = plugin.getConfiguration().GENERAL_MATERIALS.get(material);

            new BukkitRunnable() {
                @Override
                public void run() {
                    int current = plugin.getUtils().getAmountInChunk(event.getBlock().getChunk(), material);
                    if (current > limit) {
                        if (!player.hasPermission("insights.bypass." + material.name())) {
                            plugin.getUtils().sendMessage(event.getPlayer(), "messages.limit_reached_custom", "%limit%", NumberFormat.getIntegerInstance().format(limit), "%material%", plugin.getUtils().capitalizeName(material.name().toLowerCase()));
                            if (player.getGameMode() != GameMode.CREATIVE) {
                                player.getInventory().addItem(new ItemStack(event.getItemInHand()).asOne());
                            }
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    event.getBlock().setType(Material.AIR);
                                }
                            }.runTask(plugin);
                        }
                    }

                    if (event.getPlayer().hasPermission("insights.check.realtime") && plugin.getSqLite().hasRealtimeCheckEnabled(player)) {
                        double progress = ((double) current)/((double) limit);
                        if (progress > 1 || progress < 0) progress = 1;
                        plugin.getUtils().sendSpecialMessage(event.getPlayer(), "messages.realtime_check_custom", progress, "%count%", NumberFormat.getIntegerInstance().format(current), "%material%", plugin.getUtils().capitalizeName(material.name().toLowerCase()), "%limit%", NumberFormat.getIntegerInstance().format(limit));
                    }
                }
            }.runTaskAsynchronously(plugin);
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
