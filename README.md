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
[bStatsImg]: https://bstats.org/signatures/bukkit/Insights.svg
[bStats]: https://bstats.org/plugin/bukkit/Insights/7272
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

[![Discord](https://img.shields.io/discord/580773821745725452.svg?label=&logo=discord&logoColor=ffffff&color=7389D8&labelColor=6A7EC2)](https://discord.gg/WJGvzue)

[![bStatsImg]][bStats]

## How to run the project?
1. Make sure you have [gradle][gradleInstall] installed.
2. Run the project with `gradle shadowJar` to compile it with dependencies.

## Developer API
### Repository / Dependency
Please use the following maven repository:
#### Gradle:
```groovy
maven { url 'https://jitpack.io' }
```
and as dependency:
```groovy
compileOnly 'com.github.InsightsPlugin:Insights:VERSION'
```
#### Maven:
```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```
and as dependency:
```xml
<dependency>
    <groupId>com.github.InsightsPlugin</groupId>
    <artifactId>Insights</artifactId>
    <version>VERSION</version>
    <scope>provided</scope>
</dependency>
```

### Addons
See the [Insights Wiki](https://github.com/InsightsPlugin/Insights/wiki/Addon-API) on how to implement your own addon for Insights!

## Screenshots
### Limit blocks per group
![GroupLimit](screenshots/GroupLimit.png)
### Custom block limit per chunk
![CustomLimit](screenshots/CustomLimit.png)
### Scan all blocks in a radius around you!
![ScanRadius](screenshots/ScanRadius.png)
### Apply limitations to WorldEdit!
![WorldEditLimit](screenshots/WorldEditLimit.png)
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
