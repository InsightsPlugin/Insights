Insights
===========

For the description of this plugin, please refer to SpigotMC: https://www.spigotmc.org/resources/56489/

Developer API
------
```java
package net.frankheijden.insights;

import net.frankheijden.insights.api.InsightsAPI;
import net.frankheijden.insights.api.entities.ChunkLocation;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class MyClass extends JavaPlugin {
    InsightsAPI insightsAPI;

    @Override
    public void onEnable() {
        super.onEnable();

        // Create a new InsightsAPI instance
        insightsAPI = new InsightsAPI();

        // Get a world to scan in
        World world = Bukkit.getWorld("world");

        // Get some chunklocations together
        List<ChunkLocation> chunkLocations = new ArrayList<>();
        for (Chunk chunk : world.getLoadedChunks()) {
            ChunkLocation chunkLocation = new ChunkLocation(chunk.getX(), chunk.getZ());
            chunkLocations.add(chunkLocation);
        }

        // Lets scan for ores!
        List<Material> materials = Arrays.asList(
                Material.COAL_ORE,
                Material.IRON_ORE,
                Material.LAPIS_ORE,
                Material.REDSTONE_ORE,
                Material.EMERALD_ORE,
                Material.DIAMOND_ORE
        );

        // Lets also scan for Creepers ;-)
        List<EntityType> entityTypes = Collections.singletonList(EntityType.CREEPER);

        // Let the scan begin!
        CompletableFuture<TreeMap<String, Integer>> completableFuture = insightsAPI.scan(world, chunkLocations, materials, entityTypes);

        // When the scan has been completed, execute:
        completableFuture.whenCompleteAsync((counts, error) -> {
            // Print them in the console!
            System.out.println(counts.toString());
        });
    }
}
```