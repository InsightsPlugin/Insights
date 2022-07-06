package dev.frankheijden.insights.api.region;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.addons.AddonRegion;
import dev.frankheijden.insights.api.concurrent.ScanOptions;
import dev.frankheijden.insights.api.concurrent.storage.RegionStorage;
import dev.frankheijden.insights.api.concurrent.storage.Storage;
import dev.frankheijden.insights.api.concurrent.tracker.RegionScanTracker;
import dev.frankheijden.insights.api.objects.chunk.ChunkLocation;
import dev.frankheijden.insights.api.objects.chunk.ChunkPart;
import dev.frankheijden.insights.api.tasks.ScanTask;
import org.bukkit.Location;
import org.checkerframework.checker.nullness.qual.NonNull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RegionManager {

    private final InsightsPlugin plugin;
    private final RegionStorage regionStorage;
    private final RegionScanTracker regionScanTracker;

    /**
     * Constructs a new RegionManager.
     */
    public RegionManager(InsightsPlugin plugin) {
        this.plugin = plugin;
        this.regionStorage = new RegionStorage();
        this.regionScanTracker = new RegionScanTracker();
    }

    public @NonNull RegionStorage regionStorage() {
        return regionStorage;
    }

    public @NonNull RegionScanTracker regionScanTracker() {
        return regionScanTracker;
    }

    /**
     * Retrieves all regions at the given location.
     */
    public List<Region> regionsAt(Location location) {
        List<Region> regions = new ArrayList<>();
        regions.add(new ChunkRegion(ChunkLocation.of(location)));
        regions.addAll(plugin.addonManager().regionsAt(location));
        return regions;
    }

    /**
     * Scans the given region with specified options.
     */
    public CompletableFuture<Storage> scan(Region region, ScanOptions options) {
        List<ChunkPart> chunkParts = region.generateChunkParts();
        if (chunkParts.size() == 1) {
            ChunkPart chunkPart = chunkParts.get(0);
            return plugin.chunkContainerExecutor().submit(
                    chunkPart.chunkLocation(),
                    chunkPart.chunkCuboid(),
                    options
            );
        } else {
            CompletableFuture<Storage> future = new CompletableFuture<>();
            if (options.track()) regionScanTracker.setQueued(region, true);
            ScanTask.scan(plugin, chunkParts, chunkParts.size(), ScanOptions.scanOnly(), info -> {}, storage -> {
                if (options.track()) regionScanTracker.setQueued(region, false);
                if (options.save()) regionStorage.put(region, storage);
                future.complete(storage);
            });
            return future;
        }
    }

    /**
     * Retrieves the common area name of given region.
     */
    public @NonNull String areaName(Region region) {
        if (region instanceof AddonRegion addonRegion) {
            return plugin.addonManager().addonContainer(addonRegion.addonId()).addonInfo().areaName();
        } else if (region instanceof ChunkRegion) {
            return "chunk";
        } else {
            return "region";
        }
    }
}
