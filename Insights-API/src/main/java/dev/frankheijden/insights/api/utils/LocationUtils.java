package dev.frankheijden.insights.api.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class LocationUtils {

    private LocationUtils() {}

    /**
     * Serializes a location into a String.
     */
    public static String getKey(Location loc) {
        return loc.getWorld().getName() + '|' + loc.getBlockX() + '|' + loc.getBlockY() + '|' + loc.getBlockZ();
    }

    /**
     * Parses a location key to a Location object.
     */
    public static Location getKey(String key) {
        String[] split = key.split("\\|");
        return new Location(
                Bukkit.getWorld(split[0]),
                Integer.parseInt(split[1]),
                Integer.parseInt(split[2]),
                Integer.parseInt(split[3])
        );
    }
}
