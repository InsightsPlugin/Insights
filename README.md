<!-- Variables (this block will not be visible in the readme -->
[spigot]: https://www.spigotmc.org/resources/56489/
[spigotRatingImg]: https://img.shields.io/badge/dynamic/json.svg?color=brightgreen&label=rating&query=%24.rating.average&suffix=%20%2F%205&url=https%3A%2F%2Fapi.spiget.org%2Fv2%2Fresources%2F56489
[spigotDownloadsImg]: https://img.shields.io/badge/dynamic/json.svg?color=brightgreen&label=downloads%20%28spigotmc.org%29&query=%24.downloads&url=https%3A%2F%2Fapi.spiget.org%2Fv2%2Fresources%2F56489
[issues]: https://github.com/FrankHeijden/Insights/issues
[wiki]: https://github.com/FrankHeijden/Insights/wiki
[release]: https://github.com/FrankHeijden/Insights/releases/latest
[releaseImg]: https://img.shields.io/github/release/FrankHeijden/Insights.svg?label=github%20release
[license]: https://github.com/FrankHeijden/Insights/blob/master/LICENSE
[licenseImg]: https://img.shields.io/github/license/FrankHeijden/Insights.svg
[ci]: https://ci.frankheijden.net/job/Insights/
[ciImg]: https://ci.frankheijden.net/job/Insights/badge/icon
[bugReports]: https://github.com/FrankHeijden/Insights/issues?q=is%3Aissue+is%3Aopen+label%3Abug
[bugReportsImg]: https://img.shields.io/github/issues/FrankHeijden/Insights/bug.svg?label=bug%20reports
[reportBug]: https://github.com/FrankHeijden/Insights/issues/new?labels=bug&template=bug.md
[featureRequests]: https://github.com/FrankHeijden/Insights/issues?q=is%3Aissue+is%3Aopen+label%3Aenhancement
[featureRequestsImg]: https://img.shields.io/github/issues/FrankHeijden/Insights/enhancement.svg?label=feature%20requests&color=informational
[requestFeature]: https://github.com/FrankHeijden/Insights/issues/new?labels=enhancement&template=feature.md
[config]: https://github.com/FrankHeijden/Insights/blob/master/resources/config.yml
<!-- End of variables block -->


Insights
===========

For the description of this plugin, please refer to SpigotMC: https://www.spigotmc.org/resources/56489/

[![ciImg]][ci] [![releaseImg]][release] [![licenseImg]][license]

[![featureRequestsImg]][featureRequests] [![bugReportsImg]][bugReports]
[![spigotRatingImg]][spigot] [![spigotDownloadsImg]][spigot]

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
        CompletableFuture<ScanCompleteEvent> completableFuture = insightsAPI.scan(world, chunkLocations, materials, entityTypes);

        // When the scan has been completed, execute:
        completableFuture.whenCompleteAsync((event, error) -> {
            // Print them in the console!
            System.out.println(event.getCounts().toString());
        });
    }
}
```