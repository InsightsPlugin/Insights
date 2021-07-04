package dev.frankheijden.insights.api.concurrent.containers;

import dev.frankheijden.insights.api.concurrent.ScanOptions;
import dev.frankheijden.insights.api.objects.chunk.ChunkCuboid;
import dev.frankheijden.insights.api.reflection.RCraftChunk;
import net.minecraft.world.level.chunk.ChunkSection;
import org.bukkit.Chunk;

public class LoadedChunkContainer extends ChunkContainer {

    private final Chunk chunk;

    /**
     * Constructs a new LoadedChunkContainer, for scanning of a loaded chunk.
     */
    public LoadedChunkContainer(Chunk chunk, ChunkCuboid cuboid, ScanOptions options, ContainerPriority priority) {
        super(chunk.getWorld(), chunk.getX(), chunk.getZ(), cuboid, options, priority);

        this.chunk = chunk;
    }

    @Override
    public ChunkSection[] getChunkSections() {
        net.minecraft.world.level.chunk.Chunk nmsChunk = RCraftChunk.getReflection().invoke(chunk, "getHandle");
        return nmsChunk.getSections();
    }
}
