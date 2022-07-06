package dev.frankheijden.insights.api.listeners;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.addons.AddonRegion;
import dev.frankheijden.insights.api.region.Region;
import dev.frankheijden.insights.api.concurrent.ScanOptions;
import dev.frankheijden.insights.api.concurrent.storage.Storage;
import dev.frankheijden.insights.api.config.LimitEnvironment;
import dev.frankheijden.insights.api.config.Messages;
import dev.frankheijden.insights.api.config.limits.Limit;
import dev.frankheijden.insights.api.config.limits.LimitInfo;
import dev.frankheijden.insights.api.objects.InsightsBase;
import dev.frankheijden.insights.api.objects.wrappers.ScanObject;
import dev.frankheijden.insights.api.region.RegionManager;
import dev.frankheijden.insights.api.util.Triplet;
import dev.frankheijden.insights.api.utils.StringUtils;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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

    protected void handleModification(Location location, Consumer<Storage> storageConsumer) {
        var regionManager = plugin.regionManager();
        for (Region region : regionManager.regionsAt(location)) {
            Storage storage = regionManager.regionStorage().get(region);
            if (storage == null) continue;
            storageConsumer.accept(storage);
        }
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

    protected boolean handleModification(
            Player player,
            Location location,
            ScanObject<?> item,
            int delta,
            boolean included
    ) {
        return handleModification(player, plugin.regionManager().regionsAt(location), item, delta, included);
    }

    protected boolean handleModification(
            Player player,
            Collection<? extends Region> regions,
            ScanObject<?> item,
            int delta,
            boolean included
    ) {
        if (delta < 0 && !player.hasPermission("insights.notifications")) return false;

        RegionManager regionManager = plugin.regionManager();
        List<Region> queued = new ArrayList<>(regions.size());
        List<Region> scanning = new ArrayList<>(regions.size());
        for (Region region : regions) {
            LimitEnvironment env = new LimitEnvironment(player, Collections.singletonList(region));
            Limit limit = plugin.limits().firstLimit(item, env);
            if (limit == null) continue;

            LimitInfo limitInfo = limit.limitInfo(item);

            // Check if the limit allows placement outside addon regions
            if (delta > 0 && !(region instanceof AddonRegion) && limit.settings().disallowedPlacementOutsideRegion()) {
                plugin.messages()
                        .getMessage(Messages.Key.LIMIT_DISALLOWED_PLACEMENT)
                        .addTemplates(TagResolver.resolver(
                                Messages.tagOf("name", limitInfo.name()),
                                Messages.tagOf("area", regionManager.areaName(region))
                        ))
                        .sendTo(player);
                return true;
            }

            // Check if it is queued
            if (regionManager.regionScanTracker().isQueued(region)) {
                queued.add(region);
            }

            // Check if it needs scanning
            Storage storage = regionManager.regionStorage().get(region);
            if (storage == null) {
                scanning.add(region);
                continue;
            }

            if (delta < 0) {
                storage.modify(item, delta);
            }

            long count = storage.count(limit, item);

            // If count is beyond limit, act
            if (delta > 0 && count + delta > limitInfo.limit()) {
                plugin.messages().getMessage(Messages.Key.LIMIT_REACHED).addTemplates(
                        Messages.tagOf("limit", StringUtils.pretty(limitInfo.limit())),
                        Messages.tagOf("name", limitInfo.name()),
                        Messages.tagOf("area", regionManager.areaName(region))
                ).sendTo(player);
                return true;
            }
        }

        // Return if scan(s) are in progress
        if (scanning.size() > 0) {
            // Submit futures
            CompletableFuture<?>[] scanFutures = new CompletableFuture[scanning.size()];
            for (int i = 0; i < scanFutures.length; i++) {
                Region region = scanning.get(i);
                scanFutures[i] = regionManager
                        .scan(region, ScanOptions.all())
                        .thenAccept(storage -> {
                            // Subtract item if it was included in the scan, because the event was cancelled.
                            if (included) {
                                boolean addonRegion = region instanceof AddonRegion;

                                // Only iff the block was included in the chunk AND its not a cuboid/area scan.
                                if ((delta > 0 && addonRegion) || (delta < 0 && !addonRegion)) {
                                    storage.modify(item, -delta);
                                }
                            }
                        });
            }

            CompletableFuture<Void> allFuture = CompletableFuture.allOf(scanFutures);
            if (delta > 0 && plugin.settings().canReceiveAreaScanNotifications(player)) {
                // Broadcast start
                plugin.messages()
                        .getMessage(Messages.Key.AREA_SCAN_STARTED)
                        .addTemplates(
                                Messages.tagOf(
                                        "area",
                                        String.join(", ", scanning.stream().map(regionManager::areaName).toList())
                                )
                        )
                        .sendTo(player);

                // Broadcast end
                allFuture.thenRun(() -> plugin.messages()
                        .getMessage(Messages.Key.AREA_SCAN_COMPLETED)
                        .sendTo(player));
            } else if (delta < 0) {
                allFuture.thenRun(() -> evaluateModification(player, regions, item, delta));
            }
            return true;
        } else if (queued.size() > 0) {
            if (plugin.settings().canReceiveAreaScanNotifications(player)) {
                plugin.messages()
                        .getMessage(Messages.Key.AREA_SCAN_QUEUED)
                        .addTemplates(
                                Messages.tagOf(
                                        "area",
                                        String.join(", ", queued.stream().map(regionManager::areaName).toList())
                                )
                        )
                        .sendTo(player);
            }
            return true;
        }
        return false;
    }

    protected void evaluateModification(Player player, Location location, ScanObject<?> item, int delta) {
        evaluateModification(player, plugin.regionManager().regionsAt(location), item, delta);
    }

    protected void evaluateModification(
            Player player,
            Collection<? extends Region> regions,
            ScanObject<?> item,
            int delta
    ) {
        // Notify the user (if they have permission)
        if (!player.hasPermission("insights.notifications")) return;

        Triplet<Region, Limit, Storage> smallestLimit = plugin.limits().smallestLimit(player, regions, item, delta);
        if (smallestLimit != null) {
            Limit limit = smallestLimit.b();
            LimitInfo limitInfo = limit.limitInfo(item);
            long count = smallestLimit.c().count(limit, item) + delta;
            float progress = (float) count / limitInfo.limit();
            plugin.notifications().getCachedProgress(player.getUniqueId(), Messages.Key.LIMIT_NOTIFICATION)
                    .progress(progress)
                    .add(player)
                    .create()
                    .addTemplates(
                            Messages.tagOf("name", limitInfo.name()),
                            Messages.tagOf("count", StringUtils.pretty(count)),
                            Messages.tagOf("limit", StringUtils.pretty(limitInfo.limit()))
                    )
                    .send();
        }
    }
}
