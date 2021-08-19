package dev.frankheijden.insights.api.concurrent.containers;

import dev.frankheijden.insights.api.concurrent.ScanOptions;
import dev.frankheijden.insights.api.objects.chunk.ChunkCuboid;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.bukkit.Chunk;
import org.bukkit.craftbukkit.v1_17_R1.CraftChunk;

public class LoadedChunkContainer extends ChunkContainer {

    private final Chunk chunk;

    /**
     * Constructs a new LoadedChunkContainer, for scanning of a loaded chunk.
     */
    public LoadedChunkContainer(Chunk chunk, ChunkCuboid cuboid, ScanOptions options) {
        super(chunk.getWorld(), chunk.getX(), chunk.getZ(), cuboid, options);

        this.chunk = chunk;
    }

    @Override
    public LevelChunkSection[] getChunkSections() {
        return ((CraftChunk) chunk).getHandle().getSections();
    }
}
