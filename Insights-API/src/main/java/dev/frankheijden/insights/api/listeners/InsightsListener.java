package dev.frankheijden.insights.api.listeners;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.addons.Region;
import dev.frankheijden.insights.api.concurrent.ScanOptions;
import dev.frankheijden.insights.api.concurrent.storage.ChunkStorage;
import dev.frankheijden.insights.api.concurrent.storage.AddonStorage;
import dev.frankheijden.insights.api.concurrent.storage.DistributionStorage;
import dev.frankheijden.insights.api.concurrent.storage.Storage;
import dev.frankheijden.insights.api.concurrent.storage.WorldStorage;
import dev.frankheijden.insights.api.config.LimitEnvironment;
import dev.frankheijden.insights.api.config.Messages;
import dev.frankheijden.insights.api.config.limits.Limit;
import dev.frankheijden.insights.api.config.limits.LimitInfo;
import dev.frankheijden.insights.api.objects.InsightsBase;
import dev.frankheijden.insights.api.objects.chunk.ChunkPart;
import dev.frankheijden.insights.api.objects.wrappers.ScanObject;
import dev.frankheijden.insights.api.tasks.ScanTask;
import dev.frankheijden.insights.api.utils.ChunkUtils;
import dev.frankheijden.insights.api.utils.SchedulingUtils;
import dev.frankheijden.insights.api.utils.StringUtils;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;

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

    protected void handleModification(Location location, Consumer<Storage> storageConsumer) {
        SchedulingUtils.runImmediatelyAtLocationIfFolia(plugin, location, () -> {
            UUID worldUid = location.getWorld().getUID();
            long chunkKey = ChunkUtils.getKey(location);
            plugin.getWorldStorage().getWorld(worldUid).get(chunkKey).ifPresent(storageConsumer);
            plugin.getAddonManager().getRegion(location)
                    .flatMap(region -> plugin.getAddonStorage().get(region.getKey()))
                    .ifPresent(storageConsumer);
        });
    }

    protected void handleModification(Location location, Material from, Material to, int amount) {
        handleModification(location, storage -> {
            storage.modify(ScanObject.of(from), -amount);
            storage.modify(ScanObject.of(to), amount);
        });
    }

    protected void handleModification(Location location, EntityType entity, int amount) {
        handleModification(location, storage -> storage.modify(ScanObject.of(entity), amount));
    }

    protected boolean handleAddition(Player player, Location location, ScanObject<?> item, int delta) {
        return handleAddition(player, location, item, delta, true);
    }

    protected boolean handleAddition(
            Player player,
            Location location,
            ScanObject<?> item,
            int delta,
            boolean included
    ) {
        Optional<Region> regionOptional = plugin.getAddonManager().getRegion(location);
        var chunk = location.getChunk();
        var world = location.getWorld();
        UUID worldUid = world.getUID();
        long chunkKey = ChunkUtils.getKey(chunk);

        boolean queued;
        String area;
        LimitEnvironment env;
        if (regionOptional.isPresent()) {
            var region = regionOptional.get();
            queued = plugin.getAddonScanTracker().isQueued(region.getKey());
            area = plugin.getAddonManager().getAddon(region.getAddon()).getAreaName();
            env = new LimitEnvironment(player, world.getName(), region.getAddon());
        } else {
            queued = plugin.getWorldChunkScanTracker().isQueued(worldUid, chunkKey);
            area = "chunk";
            env = new LimitEnvironment(player, world.getName());
        }

        if (queued) {
            if (plugin.getSettings().canReceiveAreaScanNotifications(player)) {
                plugin.getMessages().getMessage(Messages.Key.AREA_SCAN_QUEUED).addTemplates(
                        Messages.tagOf("area", area)
                ).sendTo(player);
            }
            return true;
        }

        // Get the first (smallest) limit for the specific user (bypass permissions taken into account)
        Optional<Limit> limitOptional = plugin.getLimits().getFirstLimit(item, env);
        if (limitOptional.isEmpty()) return false;
        var limit = limitOptional.get();
        var limitInfo = limit.getLimit(item);

        if (regionOptional.isEmpty() && limit.getSettings().isDisallowedPlacementOutsideRegion()) {
            plugin.getMessages().getMessage(Messages.Key.LIMIT_DISALLOWED_PLACEMENT).addTemplates(TagResolver.resolver(
                    Messages.tagOf("name", limitInfo.getName()),
                    Messages.tagOf("area", area)
            )).sendTo(player);
            return true;
        }

        Consumer<Storage> storageConsumer = storage -> {
            // Subtract item if it was included in the scan, because the event was cancelled.
            // Only iff the block was included in the chunk AND its not a cuboid/area scan.
            if (included && regionOptional.isEmpty()) {
                storage.modify(item, -delta);
            }

            // Notify the user scan completed
            if (plugin.getSettings().canReceiveAreaScanNotifications(player)) {
                plugin.getMessages().getMessage(Messages.Key.AREA_SCAN_COMPLETED).sendTo(player);
            }
        };

        Optional<Storage> storageOptional;
        if (regionOptional.isPresent()) {
            storageOptional = handleAddonAddition(player, regionOptional.get(), storageConsumer);
        } else {
            storageOptional = handleChunkAddition(player, chunk, storageConsumer);
        }

        // If the storage is not present, cancel.
        if (storageOptional.isEmpty()) return true;

        var storage = storageOptional.get();
        long count = storage.count(limit, item);

        // If count is beyond limit, act
        if (count + delta > limitInfo.getLimit()) {
            plugin.getMessages().getMessage(Messages.Key.LIMIT_REACHED).addTemplates(
                    Messages.tagOf("limit", StringUtils.pretty(limitInfo.getLimit())),
                    Messages.tagOf("name", limitInfo.getName()),
                    Messages.tagOf("area", area)
            ).sendTo(player);
            return true;
        }
        return false;
    }

    protected void evaluateAddition(Player player, Location location, ScanObject<?> item, int delta) {
        Optional<Region> regionOptional = plugin.getAddonManager().getRegion(location);
        World world = location.getWorld();
        long chunkKey = ChunkUtils.getKey(location);
        UUID uuid = player.getUniqueId();

        LimitEnvironment env;
        Optional<Storage> storageOptional;
        if (regionOptional.isPresent()) {
            Region region = regionOptional.get();
            env = new LimitEnvironment(player, world.getName(), region.getAddon());
            storageOptional = plugin.getAddonStorage().get(region.getKey());
        } else {
            env = new LimitEnvironment(player, world.getName());
            storageOptional = plugin.getWorldStorage().getWorld(world.getUID()).get(chunkKey);
        }

        if (storageOptional.isEmpty()) return;
        Storage storage = storageOptional.get();

        // If limit is not present, stop here
        Optional<Limit> limitOptional = plugin.getLimits().getFirstLimit(item, env);
        if (limitOptional.isEmpty()) return;

        Limit limit = limitOptional.get();
        LimitInfo limitInfo = limit.getLimit(item);
        long count = storage.count(limit, item);

        // Notify the user (if they have permission)
        if (player.hasPermission("insights.notifications")) {
            float progress = (float) (count + delta) / limitInfo.getLimit();
            plugin.getNotifications().getCachedProgress(uuid, Messages.Key.LIMIT_NOTIFICATION)
                    .progress(progress)
                    .add(player)
                    .create()
                    .addTemplates(
                            Messages.tagOf("name", limitInfo.getName()),
                            Messages.tagOf("count", StringUtils.pretty(count + delta)),
                            Messages.tagOf("limit", StringUtils.pretty(limitInfo.getLimit()))
                    )
                    .send();
        }
    }

    private Optional<Storage> handleChunkAddition(
            Player player,
            Chunk chunk,
            Consumer<Storage> storageConsumer
    ) {
        UUID worldUid = chunk.getWorld().getUID();
        long chunkKey = ChunkUtils.getKey(chunk);

        WorldStorage worldStorage = plugin.getWorldStorage();
        ChunkStorage chunkStorage = worldStorage.getWorld(worldUid);
        Optional<Storage> storageOptional = chunkStorage.get(chunkKey);

        // If the chunk is not known
        if (storageOptional.isEmpty()) {
            // Notify the user scan started
            if (plugin.getSettings().canReceiveAreaScanNotifications(player)) {
                plugin.getMessages().getMessage(Messages.Key.AREA_SCAN_STARTED).addTemplates(
                        Messages.tagOf("area", "chunk")
                ).sendTo(player);
            }

            // Submit the chunk for scanning
            plugin.getChunkContainerExecutor().submit(chunk)
                    .thenAccept(storageConsumer)
                    .exceptionally(th -> {
                        plugin.getLogger().log(Level.SEVERE, th, th::getMessage);
                        return null;
                    });
        }
        return storageOptional;
    }

    private Optional<Storage> handleAddonAddition(
            Player player,
            Region region,
            Consumer<Storage> storageConsumer
    ) {
        String key = region.getKey();

        AddonStorage addonStorage = plugin.getAddonStorage();
        Optional<Storage> storageOptional = addonStorage.get(key);
        if (storageOptional.isEmpty()) {
            // Notify the user scan started
            if (plugin.getSettings().canReceiveAreaScanNotifications(player)) {
                plugin.getMessages().getMessage(Messages.Key.AREA_SCAN_STARTED).addTemplates(
                        Messages.tagOf("area", plugin.getAddonManager().getAddon(region.getAddon()).getAreaName())
                ).sendTo(player);
            }

            scanRegion(player, region, storageConsumer);
            return Optional.empty();
        }
        return storageOptional;
    }

    protected void handleRemoval(Player player, Location location, ScanObject<?> item, int delta) {
        handleRemoval(player, location, item, delta, true);
    }

    protected void handleRemoval(Player player, Location location, ScanObject<?> item, int delta, boolean included) {
        Optional<Region> regionOptional = plugin.getAddonManager().getRegion(location);
        Chunk chunk = location.getChunk();
        World world = location.getWorld();
        UUID worldUid = world.getUID();
        long chunkKey = ChunkUtils.getKey(chunk);
        UUID uuid = player.getUniqueId();

        boolean queued;
        LimitEnvironment env;
        Optional<Storage> storageOptional;
        if (regionOptional.isPresent()) {
            Region region = regionOptional.get();
            queued = plugin.getAddonScanTracker().isQueued(region.getKey());
            env = new LimitEnvironment(player, world.getName(), region.getAddon());
            storageOptional = plugin.getAddonStorage().get(region.getKey());
        } else {
            queued = plugin.getWorldChunkScanTracker().isQueued(worldUid, chunkKey);
            env = new LimitEnvironment(player, world.getName());
            storageOptional = plugin.getWorldStorage().getWorld(worldUid).get(chunkKey);
        }

        // Modify the area to account for the broken block.
        storageOptional.ifPresent(storage -> storage.modify(item, -delta));

        // Notify the user (if they have permission)
        if (player.hasPermission("insights.notifications")) {
            // If the area is queued, stop check here (notification will be displayed when it completes).
            if (queued) return;

            // Get the first (smallest) limit for the specific user (bypass permissions taken into account)
            Optional<Limit> limitOptional = plugin.getLimits().getFirstLimit(item, env);
            if (limitOptional.isEmpty()) return;
            Limit limit = limitOptional.get();
            LimitInfo limitInfo = limit.getLimit(item);

            // Create a runnable for the notification.
            Consumer<Storage> notification = storage -> {
                long count = storage.count(limit, item);
                float progress = (float) count / limitInfo.getLimit();
                plugin.getNotifications().getCachedProgress(uuid, Messages.Key.LIMIT_NOTIFICATION)
                        .progress(progress)
                        .add(player)
                        .create()
                        .addTemplates(
                                Messages.tagOf("name", limitInfo.getName()),
                                Messages.tagOf("count", StringUtils.pretty(count)),
                                Messages.tagOf("limit", StringUtils.pretty(limitInfo.getLimit()))
                        )
                        .send();
            };

            // If the data is already stored, send the notification immediately.
            if (storageOptional.isPresent()) {
                notification.accept(storageOptional.get());
                return;
            }

            // Else, we need to scan the area first.
            Consumer<Storage> storageConsumer = storage -> {
                // Subtract the broken block, as the first modification failed (we had to scan the chunk)
                // Only if we're not scanning a cuboid (iff cuboid, the block is already removed from the chunk)
                if (included && regionOptional.isEmpty()) storage.modify(item, -delta);

                // Notify the user
                notification.accept(storage);
            };

            if (regionOptional.isPresent()) {
                scanRegion(player, regionOptional.get(), storageConsumer);
            } else {
                plugin.getChunkContainerExecutor().submit(chunk)
                        .thenAccept(storageConsumer)
                        .exceptionally(th -> {
                            plugin.getLogger().log(Level.SEVERE, th, th::getMessage);
                            return null;
                        });
            }
        }
    }

    private void scanRegion(Player player, Region region, Consumer<Storage> storageConsumer) {
        // Submit the cuboid for scanning
        plugin.getAddonScanTracker().add(region.getAddon());
        List<ChunkPart> chunkParts = region.toChunkParts();
        ScanTask.scan(
                plugin,
                player,
                chunkParts,
                chunkParts.size(),
                ScanOptions.scanOnly(),
                player.hasPermission("insights.notifications"),
                DistributionStorage::new,
                (storage, loc, acc) -> storage.mergeRight(acc),
                storage -> {
                    plugin.getAddonScanTracker().remove(region.getAddon());

                    // Store the cuboid
                    plugin.getAddonStorage().put(region.getKey(), storage);

                    // Give the result back to the consumer
                    storageConsumer.accept(storage);
                }
        );
    }
}
