package dev.frankheijden.insights.listeners;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.listeners.InsightsListener;
import dev.frankheijden.insights.api.utils.ChunkUtils;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.ChunkUnloadEvent;

public class ChunkListener extends InsightsListener {

    public ChunkListener(InsightsPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        Chunk chunk = event.getChunk();
        plugin.getWorldStorage().getWorld(chunk.getWorld().getUID()).remove(ChunkUtils.getKey(chunk));
    }
}
