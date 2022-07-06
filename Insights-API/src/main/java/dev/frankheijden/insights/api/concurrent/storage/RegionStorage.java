package dev.frankheijden.insights.api.concurrent.storage;

import dev.frankheijden.insights.api.region.ChunkRegion;
import dev.frankheijden.insights.api.region.Region;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RegionStorage {

    private final Map<UUID, Map<UUID, Storage>> worldRegionMap;
    private final Map<UUID, Set<Long>> worldChunkMap;

    public RegionStorage() {
        this.worldRegionMap = new ConcurrentHashMap<>();
        this.worldChunkMap = new ConcurrentHashMap<>();
    }

    private @NonNull Map<UUID, Storage> worldRegionMap(@NonNull UUID worldUuid) {
        return worldRegionMap.computeIfAbsent(worldUuid, k -> new ConcurrentHashMap<>());
    }

    public @NonNull Set<Long> worldChunkSet(@NonNull UUID worldUuid) {
        return worldChunkMap.computeIfAbsent(worldUuid, k -> ConcurrentHashMap.newKeySet());
    }

    public @Nullable Storage get(@NonNull Region region) {
        return worldRegionMap(region.worldUuid()).get(region.regionUuid());
    }

    /**
     * Stores a storage object for the given region.
     */
    public void put(@NonNull Region region, @NonNull Storage storage) {
        var worldUuid = region.worldUuid();
        var regionUuid = region.regionUuid();
        worldRegionMap(region.worldUuid()).put(regionUuid, storage);
        if (region instanceof ChunkRegion) {
            worldChunkSet(worldUuid).add(regionUuid.getLeastSignificantBits());
        }
    }

    public void remove(@NonNull Region region) {
        worldRegionMap(region.worldUuid()).remove(region.regionUuid());
    }
}
