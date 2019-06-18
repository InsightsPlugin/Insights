package net.frankheijden.insights.api.events;

import net.frankheijden.insights.api.entities.ChunkLocation;
import net.frankheijden.insights.tasks.LoadChunksTask;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

import java.util.List;
import java.util.TreeMap;

public class ScanCompleteEvent {
    private TreeMap<String, Integer> counts;
    private World world;
    private List<ChunkLocation> chunkLocations;
    private List<Material> materials;
    private List<EntityType> entityTypes;

    /**
     * Event which is called when a scan has been completed and the listener
     * class confirms to the ScanCompleteEventListener interface.
     *
     * @param loadChunksTask LoadChunksTask
     */
    public ScanCompleteEvent(LoadChunksTask loadChunksTask) {
        this.counts = loadChunksTask.getScanChunksTask().getCounts();
        this.world = loadChunksTask.getWorld();
        this.chunkLocations = loadChunksTask.getChunkLocations();
        this.materials = loadChunksTask.getMaterials();
        this.entityTypes = loadChunksTask.getEntityTypes();
    }

    /**
     * Gets the amount of entries found while scanning.
     *
     * @return Key = Material and EntityType, Value = Integer count
     */
    public TreeMap<String, Integer> getCounts() {
        return counts;
    }

    /**
     * Gets the world in which we scanned.
     *
     * @return World
     */
    public World getWorld() {
        return world;
    }

    /**
     * Gets the locations which we scanned.
     *
     * @return List of ChunkLocation
     */
    public List<ChunkLocation> getChunkLocations() {
        return chunkLocations;
    }

    /**
     * Gets the materials which we have scanned.
     *
     * @return List of Material
     */
    public List<Material> getMaterials() {
        return materials;
    }

    /**
     * Gets the entities which we have scanned.
     *
     * @return List of EntityType
     */
    public List<EntityType> getEntityTypes() {
        return entityTypes;
    }
}
