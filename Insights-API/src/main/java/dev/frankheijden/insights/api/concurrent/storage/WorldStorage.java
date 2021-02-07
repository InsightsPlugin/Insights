package dev.frankheijden.insights.api.concurrent.storage;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WorldStorage {

    private final Map<UUID, ChunkStorage> chunkMap;

    public WorldStorage() {
        this.chunkMap = new ConcurrentHashMap<>();
    }

    public ChunkStorage getWorld(UUID worldUid) {
        return chunkMap.computeIfAbsent(worldUid, k -> new ChunkStorage());
    }
}
