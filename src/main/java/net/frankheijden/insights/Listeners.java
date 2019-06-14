package net.frankheijden.insights;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.HashMap;

public class Listeners implements Listener {
    private Insights plugin;

    Listeners(Insights plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (!plugin.config.GENERAL_WORLDS.contains(player.getWorld().getName())) {
            return;
        }

        Material material = event.getBlock().getType();
        if (plugin.config.GENERAL_MATERIALS.keySet().contains(material)) {
            int limit = plugin.config.GENERAL_MATERIALS.get(material);
            int current = plugin.utils.updateCachedAmountInChunk(event.getBlock().getChunk(), material, true);

            if (player.hasPermission("insights.check.realtime") && plugin.sqLite.hasRealtimeCheckEnabled(player)) {
                double progress = ((double) current)/((double) limit);
                if (progress > 1) progress = 1;
                plugin.utils.sendSpecialMessage(player, "messages.realtime_check_custom", progress, "%count%", String.valueOf(current), "%material%", plugin.utils.capitalizeName(material.name().toLowerCase()), "%limit%", String.valueOf(limit));
            }
            return;
        }

        if (plugin.utils.isTile(event.getBlock())) {
            if (player.hasPermission("insights.check.realtime") && plugin.sqLite.hasRealtimeCheckEnabled(player)) {
                int current = event.getBlock().getLocation().getChunk().getTileEntities().length - 1;
                int limit = plugin.config.GENERAL_LIMIT;
                double progress = ((double) current)/((double) limit);
                if (progress > 1) progress = 1;
                plugin.utils.sendSpecialMessage(player, "messages.realtime_check", progress, "%tile_count%", String.valueOf(current));
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        if (!plugin.config.GENERAL_WORLDS.contains(player.getWorld().getName())) {
            return;
        }

        Material material = event.getBlock().getType();
        if (plugin.config.GENERAL_MATERIALS.keySet().contains(material)) {
            int limit = plugin.config.GENERAL_MATERIALS.get(material);
            int current = plugin.utils.updateCachedAmountInChunk(event.getBlockPlaced().getChunk(), material, false);
            if (current > limit) {
                if (!player.hasPermission("insights.bypass." + material.name())) {
                    String n = event.getBlockPlaced().getChunk().getX() + "_" + event.getBlockPlaced().getChunk().getZ();
                    HashMap<Material, Integer> ms = plugin.chunkSnapshotHashMap.get(n);
                    ms.put(material, limit);
                    plugin.chunkSnapshotHashMap.put(n, ms);
                    current = current - 1;

                    plugin.utils.sendMessage(event.getPlayer(), "messages.limit_reached_custom", "%limit%", String.valueOf(limit), "%material%", plugin.utils.capitalizeName(material.name().toLowerCase()));
                    event.setCancelled(true);
                }
            }

            if (event.getPlayer().hasPermission("insights.check.realtime") && plugin.sqLite.hasRealtimeCheckEnabled(player)) {
                double progress = ((double) current)/((double) limit);
                if (progress > 1) progress = 1;
                plugin.utils.sendSpecialMessage(event.getPlayer(), "messages.realtime_check_custom", progress, "%count%", String.valueOf(current), "%material%", plugin.utils.capitalizeName(material.name().toLowerCase()), "%limit%", String.valueOf(limit));
            }
            return;
        }

        if (plugin.utils.isTile(event.getBlockPlaced())) {
            int current = event.getBlock().getLocation().getChunk().getTileEntities().length + 1;
            int limit = plugin.config.GENERAL_LIMIT;
            if (limit > -1 && current >= limit) {
                if (!player.hasPermission("insights.bypass")) {
                    event.setCancelled(true);
                    plugin.utils.sendMessage(player, "messages.limit_reached", "%limit%", String.valueOf(limit));
                }
            }

            if (player.hasPermission("insights.check.realtime") && plugin.sqLite.hasRealtimeCheckEnabled(player)) {
                double progress = ((double) current)/((double) limit);
                if (progress > 1) progress = 1;
                plugin.utils.sendSpecialMessage(player, "messages.realtime_check", progress, "%tile_count%", String.valueOf(current));
            }
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        plugin.chunkSnapshotHashMap.remove(event.getChunk().getX() + "_" + event.getChunk().getZ());
    }
}
