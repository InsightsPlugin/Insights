package dev.frankheijden.insights.api.listeners;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.concurrent.storage.Distribution;
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
        plugin.getWorldStorage().getWorld(worldUid).get(chunkKey).ifPresent(storage -> {
            Distribution<Material> distribution = storage.materials();
            distribution.modify(from, -amount);
            distribution.modify(to, amount);
        });
    }
}
