package net.frankheijden.insights;

import net.frankheijden.insights.tasks.UpdateCheckerTask;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.HashMap;
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
            int current = plugin.getUtils().updateCachedAmountInChunk(event.getBlock().getChunk(), material, true);

            if (player.hasPermission("insights.check.realtime") && plugin.getSqLite().hasRealtimeCheckEnabled(player)) {
                double progress = ((double) current)/((double) limit);
                if (progress > 1) progress = 1;
                plugin.getUtils().sendSpecialMessage(player, "messages.realtime_check_custom", progress, "%count%", String.valueOf(current), "%material%", plugin.getUtils().capitalizeName(material.name().toLowerCase()), "%limit%", String.valueOf(limit));
            }
            return;
        }

        if (plugin.getUtils().isTile(event.getBlock())) {
            if (player.hasPermission("insights.check.realtime") && plugin.getSqLite().hasRealtimeCheckEnabled(player)) {
                int current = event.getBlock().getLocation().getChunk().getTileEntities().length - 1;
                int limit = plugin.getConfiguration().GENERAL_LIMIT;
                double progress = ((double) current)/((double) limit);
                if (progress > 1) progress = 1;
                plugin.getUtils().sendSpecialMessage(player, "messages.realtime_check", progress, "%tile_count%", String.valueOf(current));
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
            int current = plugin.getUtils().updateCachedAmountInChunk(event.getBlockPlaced().getChunk(), material, false);
            if (current > limit) {
                if (!player.hasPermission("insights.bypass." + material.name())) {
                    String n = event.getBlockPlaced().getChunk().getX() + "_" + event.getBlockPlaced().getChunk().getZ();
                    HashMap<Material, Integer> ms = plugin.getChunkSnapshots().get(n);
                    ms.put(material, limit);
                    plugin.getChunkSnapshots().put(n, ms);
                    current = current - 1;

                    plugin.getUtils().sendMessage(event.getPlayer(), "messages.limit_reached_custom", "%limit%", String.valueOf(limit), "%material%", plugin.getUtils().capitalizeName(material.name().toLowerCase()));
                    event.setCancelled(true);
                }
            }

            if (event.getPlayer().hasPermission("insights.check.realtime") && plugin.getSqLite().hasRealtimeCheckEnabled(player)) {
                double progress = ((double) current)/((double) limit);
                if (progress > 1) progress = 1;
                plugin.getUtils().sendSpecialMessage(event.getPlayer(), "messages.realtime_check_custom", progress, "%count%", String.valueOf(current), "%material%", plugin.getUtils().capitalizeName(material.name().toLowerCase()), "%limit%", String.valueOf(limit));
            }
            return;
        }

        if (plugin.getUtils().isTile(event.getBlockPlaced())) {
            int current = event.getBlock().getLocation().getChunk().getTileEntities().length + 1;
            int limit = plugin.getConfiguration().GENERAL_LIMIT;
            if (limit > -1 && current >= limit) {
                if (!player.hasPermission("insights.bypass")) {
                    event.setCancelled(true);
                    plugin.getUtils().sendMessage(player, "messages.limit_reached", "%limit%", String.valueOf(limit));
                }
            }

            if (player.hasPermission("insights.check.realtime") && plugin.getSqLite().hasRealtimeCheckEnabled(player)) {
                double progress = ((double) current)/((double) limit);
                if (progress > 1) progress = 1;
                plugin.getUtils().sendSpecialMessage(player, "messages.realtime_check", progress, "%tile_count%", String.valueOf(current));
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
