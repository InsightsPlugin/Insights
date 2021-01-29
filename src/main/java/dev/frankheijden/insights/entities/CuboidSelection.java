package dev.frankheijden.insights.entities;

import dev.frankheijden.insights.utils.LocationUtils;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;

import java.util.Objects;

public class CuboidSelection {

    private Location pos1;
    private Location pos2;

    public CuboidSelection() {
        this.pos1 = null;
        this.pos2 = null;
    }

    public CuboidSelection(Location pos1, Location pos2) {
        this.pos1 = pos1;
        this.pos2 = pos2;
    }

    public CuboidSelection(World world, BoundingBox box) {
        this(
                new Location(world, box.getMinX(), box.getMinY(), box.getMinZ()),
                new Location(world, box.getMaxX(), box.getMaxY() - 1, box.getMaxZ())
        );
    }

    public Location getPos1() {
        return pos1;
    }

    public boolean setPos1(Location pos1) {
        if (pos1.equals(this.pos1)) return false;
        this.pos1 = pos1;
        return true;
    }

    public Location getPos2() {
        return pos2;
    }

    public boolean setPos2(Location pos2) {
        if (pos2.equals(this.pos2)) return false;
        this.pos2 = pos2;
        return true;
    }

    public boolean isValid() {
        return pos1 != null && pos2 != null;
    }

    public boolean contains(Location loc) {
        return LocationUtils.contains(pos1, pos2, loc);
    }

    public long getBlockCount() {
        if (!isValid()) return -1;
        int dx = Math.abs(pos1.getBlockX() - pos2.getBlockX()) + 1;
        int dy = Math.abs(pos1.getBlockY() - pos2.getBlockY()) + 1;
        int dz = Math.abs(pos1.getBlockZ() - pos2.getBlockZ()) + 1;
        return dx * dy * dz;
    }

    public int getChunkCount() {
        if (!isValid()) return -1;
        Chunk c1 = pos1.getChunk();
        Chunk c2 = pos2.getChunk();
        int dx = Math.abs(c1.getX() - c2.getX()) + 1;
        int dz = Math.abs(c1.getZ() - c2.getZ()) + 1;
        return dx * dz;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CuboidSelection selection = (CuboidSelection) o;
        return pos1.equals(selection.pos1) &&
                pos2.equals(selection.pos2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos1, pos2);
    }
}
