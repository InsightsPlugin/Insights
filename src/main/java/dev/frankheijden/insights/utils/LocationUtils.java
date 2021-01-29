package dev.frankheijden.insights.utils;

import org.bukkit.Location;

public class LocationUtils {

    public static Location min(Location v1, Location v2) {
        return new Location(v1.getWorld(),
                Math.min(v1.getX(), v2.getX()),
                Math.min(v1.getY(), v2.getY()),
                Math.min(v1.getZ(), v2.getZ())
        );
    }

    public static Location max(Location v1, Location v2) {
        return new Location(v1.getWorld(),
                Math.max(v1.getX(), v2.getX()),
                Math.max(v1.getY(), v2.getY()),
                Math.max(v1.getZ(), v2.getZ())
        );
    }

    public static boolean contains(Location v1, Location v2, Location loc) {
        Location min = min(v1, v2);
        Location max = max(v1, v2);
        return min.getWorld().equals(loc.getWorld())
                && min.getX() <= loc.getX() && max.getX() >= loc.getX()
                && min.getY() <= loc.getY() && max.getY() >= loc.getY()
                && min.getZ() <= loc.getZ() && max.getZ() >= loc.getZ();
    }

    public static String toString(Location loc) {
        return "World: " + loc.getWorld().getName()
                + ", X: " + loc.getBlockX()
                + ", Y: " + loc.getBlockY()
                + ", Z: " + loc.getBlockZ();
    }
}
