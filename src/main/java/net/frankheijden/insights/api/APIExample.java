package net.frankheijden.insights.api;

import net.frankheijden.insights.builders.Scanner;
import net.frankheijden.insights.entities.*;
import net.frankheijden.insights.enums.ScanType;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.List;

public class APIExample {

    public void performScan() {
        ScanOptions options = new ScanOptions();
        options.setScanType(ScanType.CUSTOM);

        // Let's scan in world
        World world = Bukkit.getWorld("world");
        options.setWorld(world);

        // Let's scan the whole world
        List<ChunkLocation> chunkLocations = ChunkLocation.from(world.getLoadedChunks());
        options.setChunkLocations(chunkLocations);

        // Let's scan for ores!
        options.addMaterial("DIAMOND_ORE");
        options.addMaterial("EMERALD_ORE");
        options.addMaterial("GOLD_ORE");
        options.addMaterial("LAPIS_ORE");
        options.addMaterial("IRON_ORE");
        options.addMaterial("COAL_ORE");

        // Let's also scan for creepers and spiders!
        options.addEntityType("CREEPER");
        options.addEntityType("SPIDER");

        // Let's scan!
        Scanner.create(options).scan().whenComplete((event, err) -> {
            // This block is called when the scan has completed
            ScanResult result = event.getScanResult();

            // And print the result to the console
            System.out.println("Scan Result:");
            result.getCounts().forEach((key, value) -> {
                System.out.println(key + ": " + value);
            });
        });
    }
}
