package net.frankheijden.insights.utils;

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
}
