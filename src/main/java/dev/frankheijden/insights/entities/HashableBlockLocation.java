package dev.frankheijden.insights.entities;

import java.util.Objects;
import org.bukkit.Location;

public class HashableBlockLocation {

    private final String worldName;
    private final int x;
    private final int y;
    private final int z;

    public HashableBlockLocation(String worldName, int x, int y, int z) {
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static HashableBlockLocation of(Location loc) {
        return new HashableBlockLocation(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public HashableBlockLocation add(int x, int y, int z) {
        return new HashableBlockLocation(worldName, this.x + x, this.y + y, this.z + z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HashableBlockLocation that = (HashableBlockLocation) o;
        return x == that.x && y == that.y && z == that.z && worldName.equals(that.worldName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(worldName, x, y, z);
    }
}
