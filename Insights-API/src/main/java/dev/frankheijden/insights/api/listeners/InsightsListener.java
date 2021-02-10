package dev.frankheijden.insights.api.listeners;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.addons.Region;
import dev.frankheijden.insights.api.concurrent.storage.ChunkStorage;
import dev.frankheijden.insights.api.concurrent.storage.Distribution;
import dev.frankheijden.insights.api.concurrent.storage.DistributionStorage;
import dev.frankheijden.insights.api.concurrent.storage.AddonStorage;
import dev.frankheijden.insights.api.concurrent.storage.WorldStorage;
import dev.frankheijden.insights.api.config.LimitEnvironment;
import dev.frankheijden.insights.api.config.Messages;
import dev.frankheijden.insights.api.config.Settings;
import dev.frankheijden.insights.api.config.limits.Limit;
import dev.frankheijden.insights.api.config.limits.LimitInfo;
import dev.frankheijden.insights.api.objects.InsightsBase;
import dev.frankheijden.insights.api.tasks.ScanTask;
import dev.frankheijden.insights.api.utils.ChunkUtils;
import dev.frankheijden.insights.api.utils.StringUtils;
import org.bukkit.Chunk;
import org.bukkit.Location;
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
        handleModification(block.getLocation(), block.getType(), amount);
    }

    protected void handleModification(BlockState state, int amount) {
        handleModification(state.getLocation(), state.getType(), amount);
    }

    protected void handleModification(Location location, Material material, int amount) {
        if (amount < 0) {
            handleModification(location, material, Material.AIR, -amount);
        } else {
            handleModification(location, Material.AIR, material, amount);
        }
    }

    protected void handleModification(Location location, Consumer<DistributionStorage> storageConsumer) {
        Chunk chunk = location.getChunk();
        UUID worldUid = chunk.getWorld().getUID();
        long chunkKey = ChunkUtils.getKey(chunk);
        plugin.getWorldStorage().getWorld(worldUid).get(chunkKey).ifPresent(storageConsumer);
        plugin.getAddonManager().getRegion(location)
                .flatMap(region -> plugin.getAddonStorage().get(region.getKey()))
                .ifPresent(storageConsumer);
    }

    protected void handleModification(Location location, Material from, Material to, int amount) {
        handleModification(location, storage -> {
            Distribution<Material> distribution = storage.materials();
            distribution.modify(from, -amount);
            distribution.modify(to, amount);
        });
    }

    protected void handleModification(Location location, EntityType entity, int amount) {
        handleModification(location, storage -> {
            Distribution<EntityType> distribution = storage.entities();
            distribution.modify(entity, amount);
        });
    }

    protected boolean handleAddition(Player player, Location location, Object item, int delta) {
        return handleAddition(player, location, item, delta, true);
    }

    protected boolean handleAddition(Player player, Location location, Object item, int delta, boolean included) {
        Optional<Region> regionOptional = plugin.getAddonManager().getRegion(location);
        Chunk chunk = location.getChunk();
        UUID worldUid = chunk.getWorld().getUID();
        long chunkKey = ChunkUtils.getKey(chunk);

        boolean queued;
        String area;
        LimitEnvironment env;
        if (regionOptional.isPresent()) {
            Region region = regionOptional.get();
            queued = plugin.getAddonScanTracker().isQueued(region.getKey());
            area = plugin.getAddonManager().getAddon(region.getAddon()).getAreaName();
            env = new LimitEnvironment(player, worldUid, region.getAddon());
        } else {
            queued = plugin.getWorldChunkScanTracker().isQueued(worldUid, chunkKey);
            area = "chunk";
            env = new LimitEnvironment(player, worldUid);
        }

        if (queued) {
            plugin.getMessages().getMessage(Messages.Key.AREA_SCAN_QUEUED)
                    .replace("area", area)
                    .color()
                    .sendTo(player);
            return true;
        }

        // Get the first (smallest) limit for the specific user (bypass permissions taken into account)
        Optional<Limit> limitOptional = plugin.getLimits().getFirstLimit(item, env);
        if (!limitOptional.isPresent()) return false;
        Limit limit = limitOptional.get();
        LimitInfo limitInfo = limit.getLimit(item);

        Consumer<DistributionStorage> storageConsumer = storage -> {
            // Subtract item if it was included in the scan, because the event was cancelled.
            // Only iff the block was included in the chunk AND its not a cuboid/area scan.
            if (included && !regionOptional.isPresent()) {
                storage.distribution(item).modify(item, -delta);
            }

            // Notify the user scan completed
            plugin.getMessages().getMessage(Messages.Key.AREA_SCAN_COMPLETED)
                    .color()
                    .sendTo(player);
        };

        Optional<DistributionStorage> storageOptional;
        if (regionOptional.isPresent()) {
            storageOptional = handleAddonAddition(player, regionOptional.get(), storageConsumer);
        } else {
            storageOptional = handleChunkAddition(player, chunk, storageConsumer);
        }

        // If the storage is not present, cancel.
        if (!storageOptional.isPresent()) return true;

        DistributionStorage storage = storageOptional.get();
        UUID uuid = player.getUniqueId();
        int count = storage.count(limit, item);

        // If count is beyond limit, act
        if (count + delta > limitInfo.getLimit()) {
            plugin.getMessages().getMessage(Messages.Key.LIMIT_REACHED)
                    .replace(
                            "limit", StringUtils.pretty(limitInfo.getLimit()),
                            "name", limitInfo.getName(),
                            "area", area
                    )
                    .color()
                    .sendTo(player);
            return true;
        }

        // Else notify the user (if they have permission)
        if (player.hasPermission("insights.notifications")) {
            double progress = (double) (count + delta) / limitInfo.getLimit();
            plugin.getNotifications().getCachedProgress(uuid, Messages.Key.LIMIT_NOTIFICATION)
                    .progress(progress)
                    .add(player)
                    .create()
                    .replace(
                            "name", limitInfo.getName(),
                            "count", StringUtils.pretty(count + delta),
                            "limit", StringUtils.pretty(limitInfo.getLimit())
                    )
                    .color()
                    .send();
        }
        return false;
    }

    private Optional<DistributionStorage> handleChunkAddition(
            Player player,
            Chunk chunk,
            Consumer<DistributionStorage> storageConsumer
    ) {
        UUID worldUid = chunk.getWorld().getUID();
        long chunkKey = ChunkUtils.getKey(chunk);

        WorldStorage worldStorage = plugin.getWorldStorage();
        ChunkStorage chunkStorage = worldStorage.getWorld(worldUid);
        Optional<DistributionStorage> storageOptional = chunkStorage.get(chunkKey);

        // If the chunk is not known and ChunkScanMode is set to MODIFICATION, scan the chunk
        if (!storageOptional.isPresent()
                && plugin.getSettings().CHUNK_SCAN_MODE == Settings.ChunkScanMode.MODIFICATION) {
            // Notify the user scan started
            plugin.getMessages().getMessage(Messages.Key.AREA_SCAN_STARTED)
                    .replace("area", "chunk")
                    .color()
                    .sendTo(player);

            // Submit the chunk for scanning
            plugin.getChunkContainerExecutor().submit(chunk).thenAccept(storageConsumer);

            return Optional.empty();
        }
        return storageOptional;
    }

    private Optional<DistributionStorage> handleAddonAddition(
            Player player,
            Region region,
            Consumer<DistributionStorage> storageConsumer
    ) {
        String key = region.getKey();

        AddonStorage addonStorage = plugin.getAddonStorage();
        Optional<DistributionStorage> storageOptional = addonStorage.get(key);
        if (!storageOptional.isPresent()) {
            // Notify the user scan started
            plugin.getMessages().getMessage(Messages.Key.AREA_SCAN_STARTED)
                    .replace("area", plugin.getAddonManager().getAddon(region.getAddon()).getAreaName())
                    .color()
                    .sendTo(player);

            scanRegion(player, region, storageConsumer);
            return Optional.empty();
        }
        return storageOptional;
    }

    protected void handleRemoval(Player player, Location location, Object item, int delta) {
        Optional<Region> regionOptional = plugin.getAddonManager().getRegion(location);
        Chunk chunk = location.getChunk();
        UUID worldUid = chunk.getWorld().getUID();
        long chunkKey = ChunkUtils.getKey(chunk);
        UUID uuid = player.getUniqueId();

        boolean queued;
        LimitEnvironment env;
        Optional<DistributionStorage> storageOptional;
        if (regionOptional.isPresent()) {
            Region region = regionOptional.get();
            queued = plugin.getAddonScanTracker().isQueued(region.getKey());
            env = new LimitEnvironment(player, worldUid, region.getAddon());
            storageOptional = plugin.getAddonStorage().get(region.getKey());
        } else {
            queued = plugin.getWorldChunkScanTracker().isQueued(worldUid, chunkKey);
            env = new LimitEnvironment(player, worldUid);
            storageOptional = plugin.getWorldStorage().getWorld(worldUid).get(chunkKey);
        }

        // Modify the area to account for the broken block.
        storageOptional.ifPresent(storage -> storage.distribution(item).modify(item, -delta));

        // Notify the user (if they have permission)
        if (player.hasPermission("insights.notifications")) {
            // If the area is queued, stop check here (notification will be displayed when it completes).
            if (queued) return;

            // Get the first (smallest) limit for the specific user (bypass permissions taken into account)
            Optional<Limit> limitOptional = plugin.getLimits().getFirstLimit(item, env);
            if (!limitOptional.isPresent()) return;
            Limit limit = limitOptional.get();
            LimitInfo limitInfo = limit.getLimit(item);

            // Create a runnable for the notification.
            Consumer<DistributionStorage> notification = storage -> {
                int count = storage.count(limit, item);
                double progress = (double) count / limitInfo.getLimit();
                plugin.getNotifications().getCachedProgress(uuid, Messages.Key.LIMIT_NOTIFICATION)
                        .progress(progress)
                        .add(player)
                        .create()
                        .replace(
                                "name", limitInfo.getName(),
                                "count", StringUtils.pretty(count),
                                "limit", StringUtils.pretty(limitInfo.getLimit())
                        )
                        .color()
                        .send();
            };

            // If the data is already stored, send the notification immediately.
            if (storageOptional.isPresent()) {
                notification.accept(storageOptional.get());
                return;
            }

            // Else, we need to scan the area first.
            Consumer<DistributionStorage> storageConsumer = storage -> {
                // Subtract the broken block, as the first modification failed (we had to scan the chunk)
                // Only if we're not scanning a cuboid (iff cuboid, the block is already removed from the chunk)
                if (!regionOptional.isPresent()) storage.distribution(item).modify(item, -delta);

                // Notify the user
                notification.accept(storage);
            };

            if (regionOptional.isPresent()) {
                scanRegion(player, regionOptional.get(), storageConsumer);
            } else {
                plugin.getChunkContainerExecutor().submit(chunk).thenAccept(storageConsumer);
            }
        }
    }

    private void scanRegion(Player player, Region region, Consumer<DistributionStorage> storageConsumer) {
        // Submit the cuboid for scanning
        plugin.getAddonScanTracker().add(region.getAddon());
        ScanTask.scan(plugin, player, region.toChunkParts(), storage -> {
            plugin.getAddonScanTracker().remove(region.getAddon());

            // Store the cuboid
            plugin.getAddonStorage().put(region.getKey(), storage);

            // Give the result back to the consumer
            storageConsumer.accept(storage);
        });
    }
}
