package net.frankheijden.insights.api;

import net.frankheijden.insights.Insights;
import net.frankheijden.insights.api.entities.ChunkLocation;
import net.frankheijden.insights.tasks.ScanTask;
import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

public class InsightsAPI {
    /**
     * Initiates a new InsightsAPI instance.
     */
    public InsightsAPI() {}

    /**
     * Gets the instance of Insights.
     *
     * @return Insights Main class
     */
    public Insights getInstance() {
        return Insights.getInstance();
    }

    /**
     * Scans chunks for all Materials and EntityTypes.
     *
     * @param world World in which we should scan
     * @param chunkLocations List of ChunkLocation to scan in
     * @return CompletableFuture which supplies the counts.
     */
    public CompletableFuture<TreeMap<String, Integer>> scan(World world, List<ChunkLocation> chunkLocations) {
        return scan(world, chunkLocations, null, null);
    }

    /**
     * Scans chunks for Materials and EntityTypes.
     *
     * @param world World in which we should scan
     * @param chunkLocations List of ChunkLocation to scan in
     * @param materials List of Material to scan for, null if none
     * @param entityTypes List of EntityType to scan for, null if none
     * @return CompletableFuture which supplies the counts.
     */
    public CompletableFuture<TreeMap<String, Integer>> scan(World world, List<ChunkLocation> chunkLocations, List<Material> materials, List<EntityType> entityTypes) {
        return CompletableFuture.supplyAsync(() -> {
            Object LOCK = new Object();

            String k = RandomStringUtils.randomAlphanumeric(16);
            while (getInstance().countsMap.containsKey(k)) {
                k = RandomStringUtils.randomAlphanumeric(16);
            }
            final String key = k;

            ScanTask scanTask = new ScanTask(getInstance(), world, chunkLocations, materials, entityTypes, (event) -> {
                getInstance().countsMap.put(key, event.getCounts());

                synchronized (LOCK) {
                    LOCK.notify();
                }
            });
            scanTask.start(System.currentTimeMillis());

            synchronized (LOCK) {
                try {
                    LOCK.wait(10000000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }

            return getInstance().countsMap.get(key);
        });
    }
}
