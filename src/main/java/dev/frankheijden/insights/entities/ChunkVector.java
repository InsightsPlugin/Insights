package dev.frankheijden.insights.entities;

import org.bukkit.Location;

public class ChunkVector {

    private final int x;
    private final int y;
    private final int z;

    public ChunkVector(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public static ChunkVector from(Location loc) {
        int x = loc.getBlockX() % 16;
        if (x < 0) x += 16;
        int z = loc.getBlockZ() % 16;
        if (z < 0) z += 16;
        return new ChunkVector(x, loc.getBlockY(), z);
    }

    public ChunkVector min(ChunkVector other) {
        return new ChunkVector(
                Math.min(x, other.x),
                Math.min(y, other.y),
                Math.min(z, other.z)
        );
    }

    public ChunkVector max(ChunkVector other) {
        return new ChunkVector(
                Math.max(x, other.x),
                Math.max(y, other.y),
                Math.max(z, other.z)
        );
    }

    public int count(ChunkVector other) {
        int dx = Math.abs(x - other.getX()) + 1;
        int dy = Math.abs(y - other.getY()) + 1;
        int dz = Math.abs(z - other.getZ()) + 1;
        return dx * dy * dz;
    }

    @Override
    public String toString() {
        return "ChunkVector{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
