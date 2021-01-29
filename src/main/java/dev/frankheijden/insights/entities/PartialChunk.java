package dev.frankheijden.insights.entities;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PartialChunk {

    private ChunkLocation chunkLocation;
    private ChunkVector minimum;
    private ChunkVector maximum;

    public PartialChunk(ChunkLocation chunkLocation, ChunkVector v1, ChunkVector v2) {
        this.chunkLocation = chunkLocation;
        this.minimum = v1.min(v2);
        this.maximum = v1.max(v2);
    }

    public static PartialChunk from(Chunk chunk) {
        return from(chunk.getWorld(), ChunkLocation.from(chunk));
    }

    public static PartialChunk from(World world, ChunkLocation location) {
        return new PartialChunk(
                location,
                new ChunkVector(0, 0, 0),
                new ChunkVector(15, world.getMaxHeight() - 1, 15)
        );
    }

    public static List<PartialChunk> from(World world, Collection<? extends ChunkLocation> locations) {
        return from(world, locations.toArray(new ChunkLocation[0]));
    }

    public static List<PartialChunk> from(World world, ChunkLocation... locations) {
        List<PartialChunk> partials = new ArrayList<>();
        for (ChunkLocation loc : locations) {
            partials.add(PartialChunk.from(world, loc));
        }
        return partials;
    }

    public static List<PartialChunk> from(Chunk... chunks) {
        return Stream.of(chunks)
                .map(c -> from(c.getWorld(), ChunkLocation.from(c)))
                .collect(Collectors.toList());
    }

    public ChunkLocation getChunkLocation() {
        return chunkLocation;
    }

    public void setChunkLocation(ChunkLocation chunkLocation) {
        this.chunkLocation = chunkLocation;
    }

    public ChunkVector getMinimum() {
        return minimum;
    }

    public void setMinimum(ChunkVector minimum) {
        this.minimum = minimum;
    }

    public ChunkVector getMaximum() {
        return maximum;
    }

    public void setMaximum(ChunkVector maximum) {
        this.maximum = maximum;
    }

    public boolean contains(Location loc) {
        return contains(ChunkVector.from(loc));
    }

    public boolean contains(ChunkVector v) {
        return v.getX() >= minimum.getX() && v.getX() <= maximum.getX()
                && v.getY() >= minimum.getY() && v.getY() <= maximum.getY()
                && v.getZ() >= minimum.getZ() && v.getZ() <= maximum.getZ();
    }

    public int getBlockCount() {
        return minimum.count(maximum);
    }
}
