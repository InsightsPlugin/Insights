package dev.frankheijden.insights.listeners;

import dev.frankheijden.insights.Insights;
import dev.frankheijden.insights.api.listeners.InsightsListener;
import dev.frankheijden.insights.api.utils.ChunkUtils;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.ChunkUnloadEvent;

public class ChunkListener extends InsightsListener {

    protected Insights insights;

    public ChunkListener(Insights plugin) {
        super(plugin);
        this.insights = plugin;
    }

    /**
     * Cleans up any chunk data from insights when a chunk unloads.
     */
    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        Chunk chunk = event.getChunk();
        long chunkKey = ChunkUtils.getKey(chunk);
        plugin.getWorldStorage().getWorld(chunk.getWorld().getUID()).remove(chunkKey);
        insights.getRedstoneUpdateCount().remove(chunkKey);
    }
}
