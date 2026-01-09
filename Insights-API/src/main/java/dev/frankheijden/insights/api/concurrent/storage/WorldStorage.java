package dev.frankheijden.insights.api.concurrent.storage;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-world chunk cache container.
 */
public class WorldStorage {

    private static final int DEFAULT_CHUNK_CACHE_SIZE = 5000;

    private final Map<UUID, ChunkStorage> chunkMap;
    private final int chunkCacheSize;

    public WorldStorage() {
        this(DEFAULT_CHUNK_CACHE_SIZE);
    }

    public WorldStorage(int chunkCacheSize) {
        this.chunkMap = new ConcurrentHashMap<>();
        this.chunkCacheSize = chunkCacheSize;
    }

    public ChunkStorage getWorld(UUID worldUid) {
        return chunkMap.computeIfAbsent(worldUid, k -> new ChunkStorage(chunkCacheSize));
    }

    public int getChunkCacheSize() {
        return chunkCacheSize;
    }
}
