package dev.frankheijden.insights.api.listeners;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.concurrent.storage.ChunkStorage;
import dev.frankheijden.insights.api.concurrent.storage.Distribution;
import dev.frankheijden.insights.api.concurrent.storage.DistributionStorage;
import dev.frankheijden.insights.api.concurrent.storage.WorldStorage;
import dev.frankheijden.insights.api.config.LimitEnvironment;
import dev.frankheijden.insights.api.config.Messages;
import dev.frankheijden.insights.api.config.Settings;
import dev.frankheijden.insights.api.config.limits.Limit;
import dev.frankheijden.insights.api.objects.InsightsBase;
import dev.frankheijden.insights.api.utils.ChunkUtils;
import dev.frankheijden.insights.api.utils.StringUtils;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

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

    protected void handleModification(Chunk chunk, EntityType entity, int amount) {
        UUID worldUid = chunk.getWorld().getUID();
        long chunkKey = ChunkUtils.getKey(chunk);
        plugin.getWorldStorage().getWorld(worldUid).get(chunkKey).ifPresent(storage -> {
            Distribution<EntityType> distribution = storage.entities();
            distribution.modify(entity, amount);
        });
    }

    protected boolean handleAddition(Player player, Chunk chunk, Object item, int delta) {
        UUID uuid = player.getUniqueId();
        UUID worldUid = chunk.getWorld().getUID();
        long chunkKey = ChunkUtils.getKey(chunk);

        // If the chunk is queued for scanning, notify the player & cancel.
        if (plugin.getWorldChunkScanTracker().isQueued(worldUid, chunkKey)) {
            plugin.getMessages().getMessage(Messages.Key.CHUNK_SCAN_QUEUED)
                    .color()
                    .sendTo(player);
            return true;
        }

        // Create limit environment
        LimitEnvironment env = new LimitEnvironment(player, worldUid);

        // Get the first (smallest) limit for the specific user (bypass permissions taken into account)
        Optional<Limit> limitOptional = plugin.getLimits().getFirstLimit(item, env);

        WorldStorage worldStorage = plugin.getWorldStorage();
        ChunkStorage chunkStorage = worldStorage.getWorld(worldUid);
        Optional<DistributionStorage> storageOptional = chunkStorage.get(chunkKey);

        // If a limit is present, the chunk is not known, and ChunkScanMode is set to MODIFICATION, scan the chunk
        if (limitOptional.isPresent() && !storageOptional.isPresent()
                && plugin.getSettings().CHUNK_SCAN_MODE == Settings.ChunkScanMode.MODIFICATION) {
            // Notify the user scan started
            plugin.getMessages().getMessage(Messages.Key.CHUNK_SCAN_STARTED)
                    .color()
                    .sendTo(player);

            // Submit the chunk for scanning
            plugin.getChunkContainerExecutor().submit(chunk).whenComplete((storage, err) -> {
                // Subtract block from BlockPlaceEvent as it was cancelled
                // Can't subtract one from the given map, as a copied version is stored.
                storage.distribution(item).modify(item, -delta);

                // Notify the user scan completed
                plugin.getMessages().getMessage(Messages.Key.CHUNK_SCAN_COMPLETED)
                        .color()
                        .sendTo(player);
            });
            return true;
        }

        if (limitOptional.isPresent() && storageOptional.isPresent()) {
            Limit limit = limitOptional.get();
            DistributionStorage storage = storageOptional.get();
            int count = storage.count(limit, item);

            // If count is beyond limit, act
            if (count + delta > limit.getLimit()) {
                plugin.getMessages().getMessage(Messages.Key.LIMIT_REACHED)
                        .replace(
                                "limit", StringUtils.pretty(limit.getLimit()),
                                "name", limit.getName(),
                                "area", "chunk"
                        )
                        .color()
                        .sendTo(player);
                return true;
            }

            // Else notify the user (if they have permission)
            if (player.hasPermission("insights.notifications")) {
                double progress = (double) (count + delta) / limit.getLimit();
                plugin.getNotifications().getCachedProgress(uuid, Messages.Key.LIMIT_NOTIFICATION)
                        .progress(progress)
                        .add(player)
                        .create()
                        .replace(
                                "name", limit.getName(),
                                "count", StringUtils.pretty(count + delta),
                                "limit", StringUtils.pretty(limit.getLimit())
                        )
                        .color()
                        .send();
            }
        }
        return false;
    }

    protected void handleRemoval(Player player, Chunk chunk, Object item, int delta) {
        UUID uuid = player.getUniqueId();
        UUID worldUid = chunk.getWorld().getUID();
        long chunkKey = ChunkUtils.getKey(chunk);

        // Modify the chunk to account for the broken block.
        WorldStorage worldStorage = plugin.getWorldStorage();
        ChunkStorage chunkStorage = worldStorage.getWorld(worldUid);
        Optional<DistributionStorage> storageOptional = chunkStorage.get(chunkKey);
        storageOptional.ifPresent(storage -> storage.distribution(item).modify(item, -delta));

        // Notify the user (if they have permission)
        if (player.hasPermission("insights.notifications")) {
            // If the chunk is queued, stop check here (notification will be displayed when it completes).
            if (plugin.getWorldChunkScanTracker().isQueued(worldUid, chunkKey)) {
                return;
            }

            // Create limit environment
            LimitEnvironment env = new LimitEnvironment(player, worldUid);

            // Get the first (smallest) limit for the specific user (bypass permissions taken into account)
            Optional<Limit> limitOptional = plugin.getLimits().getFirstLimit(item, env);
            if (!limitOptional.isPresent()) return;
            Limit limit = limitOptional.get();

            // Create a runnable for the notification.
            Consumer<DistributionStorage> notification = storage -> {
                int count = storage.count(limit, item);
                double progress = (double) count / limit.getLimit();
                plugin.getNotifications().getCachedProgress(uuid, Messages.Key.LIMIT_NOTIFICATION)
                        .progress(progress)
                        .add(player)
                        .create()
                        .replace(
                                "name", limit.getName(),
                                "count", StringUtils.pretty(count),
                                "limit", StringUtils.pretty(limit.getLimit())
                        )
                        .color()
                        .send();
            };

            // If the data is already stored, send the notification immediately.
            if (storageOptional.isPresent()) {
                notification.accept(storageOptional.get());
            } else { // Else, we need to scan the chunk first.
                plugin.getChunkContainerExecutor().submit(chunk).thenAccept(storage -> {
                    // Subtract the broken block, as the first modification failed (we had to scan the chunk)
                    storage.distribution(item).modify(item, -delta);

                    // Notify the user
                    notification.accept(storage);
                });
            }
        }
    }
}
