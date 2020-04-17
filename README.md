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
[bugReports]: https://github.com/FrankHeijden/Insights/issues?q=is%3Aissue+is%3Aopen+label%3Abug
[bugReportsImg]: https://img.shields.io/github/issues/FrankHeijden/Insights/bug.svg?label=bug%20reports
[reportBug]: https://github.com/FrankHeijden/Insights/issues/new?labels=bug&template=bug.md
[featureRequests]: https://github.com/FrankHeijden/Insights/issues?q=is%3Aissue+is%3Aopen+label%3Aenhancement
[featureRequestsImg]: https://img.shields.io/github/issues/FrankHeijden/Insights/enhancement.svg?label=feature%20requests&color=informational
[requestFeature]: https://github.com/FrankHeijden/Insights/issues/new?labels=enhancement&template=feature.md
[config]: https://github.com/FrankHeijden/Insights/blob/master/resources/config.yml
[gradleInstall]: https://gradle.org/install/
<!-- End of variables block -->


# Insights

For the description of this plugin, please refer to SpigotMC: https://www.spigotmc.org/resources/56489/

[![Java CI with Gradle](https://github.com/FrankHeijden/Insights/workflows/Java%20CI%20with%20Gradle/badge.svg?branch=master)](https://github.com/FrankHeijden/Insights/actions)
[![](https://jitpack.io/v/FrankHeijden/Insights.svg)](https://jitpack.io/#FrankHeijden/Insights)
[![releaseImg]][release]
[![licenseImg]][license]
[![featureRequestsImg]][featureRequests]
[![bugReportsImg]][bugReports]
[![spigotRatingImg]][spigot]
[![spigotDownloadsImg]][spigot]

## How to run the project?
1. Make sure you have [gradle][gradleInstall] installed.
2. Run the project with `gradle shadowJar` to compile it with dependencies.

## Developer API
### Scanning
An example of a scan is located [here](src/main/java/net/frankheijden/insights/api/APIExample.java).

### Useful
- Check if block/location is a tile entity: [TileUtils](src/main/java/net/frankheijden/insights/utils/TileUtils.java)
- Event for when a player moves from chunk to chunk (cancellable!): [PlayerChunkMoveEvent](src/main/java/net/frankheijden/insights/events/PlayerChunkMoveEvent.java)
- Event for when a player places down an entity (all entities): [PlayerEntityPlaceEvent](src/main/java/net/frankheijden/insights/events/PlayerEntityPlaceEvent.java)
- Event for when a player destroys an entity (all entities): [PlayerEntityDestroyEvent](src/main/java/net/frankheijden/insights/events/PlayerEntityDestroyEvent.java)

### Hooking
```java
import net.frankheijden.insights.interfaces.Hook;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

public class MyInsightsHook extends Hook {
    public MyInsightsHook(Plugin plugin) {
       super(plugin);
    }

    @Override
    public boolean shouldCancel(Block block) {
       // Cancel Insights for all Dirt blocks
       return block.getType() == Material.DIRT;
    }
}
```
```java
import net.frankheijden.insights.api.InsightsAPI;

public class MyPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
       super.onEnable();

       // Add hook
       InsightsAPI.getHookManager().addHook(new MyInsightsHook(this));
    }
}
```

## Screenshots
### Limit blocks per group
![GroupLimit](screenshots/GroupLimit.png)
### Custom block limit per chunk
![CustomLimit](screenshots/CustomLimit.png)
### Scan all blocks in a radius around you!
![ScanRadius](screenshots/ScanRadius.png)
### Limit globally all tiles per chunk!
![TileLimit](screenshots/TileLimit.png)
### Scan all tiles in chunks
![TileScan](screenshots/TileScan.png)
### Scan with custom queries
![CustomScan](screenshots/CustomScan.png)
### Automatically scan upon chunk entering
![AutoScan](screenshots/AutoScan.png)
### Disable blocks in WorldGuard regions (Regex region match)
![RegionDisallow](screenshots/RegionDisallow.png)