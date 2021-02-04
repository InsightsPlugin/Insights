package dev.frankheijden.insights.api.listeners;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.concurrent.storage.WorldDistributionStorage;
import dev.frankheijden.insights.api.objects.InsightsBase;
import dev.frankheijden.insights.api.utils.ChunkUtils;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.Listener;
import java.util.UUID;

public abstract class InsightsListener extends InsightsBase implements Listener {

    protected InsightsListener(InsightsPlugin plugin) {
        super(plugin);
    }

    protected void handleModification(Block block, int amount) {
        handleModification(block.getChunk(), block.getType(), amount);
    }

    protected void handleModification(BlockState state, int amount) {
        handleModification(state.getChunk(), state.getType(), amount);
    }

    protected void handleModification(Chunk chunk, Material material, int amount) {
        if (amount < 0) {
            handleModification(chunk, material, Material.AIR, -amount);
        } else {
            handleModification(chunk, Material.AIR, material, amount);
        }
    }

    protected void handleModification(Chunk chunk, Material from, Material to, int amount) {
        UUID worldUid = chunk.getWorld().getUID();
        long chunkKey = ChunkUtils.getKey(chunk);

        // Update the cache
        WorldDistributionStorage storage = plugin.getWorldDistributionStorage();
        System.out.println(from.name() + " " + -amount);
        System.out.println(to.name() + " " + amount);
        storage.modify(worldUid, chunkKey, from, -amount);
        storage.modify(worldUid, chunkKey, to, amount);
    }
}
