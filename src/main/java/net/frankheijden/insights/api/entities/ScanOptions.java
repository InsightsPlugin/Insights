package net.frankheijden.insights.api.entities;

import net.frankheijden.insights.api.enums.ScanType;
import net.frankheijden.insights.api.interfaces.ScanCompleteEventListener;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

import java.util.List;
import java.util.Queue;
import java.util.UUID;

public class ScanOptions {
    private ScanType scanType;
    private World world;
    private Queue<ChunkLocation> chunkLocations;
    private int chunkCount;
    private UUID uuid;
    private String path;
    private List<Material> materials;
    private List<EntityType> entityTypes;
    private boolean console;
    private boolean saveWorld;
    private boolean debug;
    private ScanCompleteEventListener listener;

    public ScanOptions(ScanType scanType, World world, Queue<ChunkLocation> chunkLocations, UUID uuid, String path, List<Material> materials, List<EntityType> entityTypes, boolean console, boolean saveWorld, boolean debug, ScanCompleteEventListener listener) {
        this.scanType = scanType;
        this.world = world;
        this.chunkLocations = chunkLocations;
        this.chunkCount = chunkLocations.size(); // Store size variable as queue is used for in-place scanning
        this.uuid = uuid;
        this.path = path;
        this.materials = (materials != null && materials.isEmpty()) ? null : materials;
        this.entityTypes = (entityTypes != null && entityTypes.isEmpty()) ? null : entityTypes;
        this.console = console;
        this.saveWorld = saveWorld;
        this.debug = debug;
        this.listener = listener;
    }

    public ScanType getScanType() {
        return scanType;
    }

    public World getWorld() {
        return world;
    }

    public Queue<ChunkLocation> getChunkLocations() {
        return chunkLocations;
    }

    public int getChunkCount() {
        return chunkCount;
    }

    public boolean hasUUID() {
        return uuid != null;
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getPath() {
        return path;
    }

    public List<Material> getMaterials() {
        return materials;
    }

    public List<EntityType> getEntityTypes() {
        return entityTypes;
    }

    public boolean isConsole() {
        return console;
    }

    public boolean shouldSaveWorld() {
        return saveWorld;
    }

    public boolean isDebug() {
        return debug;
    }

    public ScanCompleteEventListener getListener() {
        return listener;
    }
}
