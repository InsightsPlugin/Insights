package dev.frankheijden.insights.api.concurrent.containers;

import dev.frankheijden.insights.api.concurrent.ScanOptions;
import dev.frankheijden.insights.api.objects.chunk.ChunkCuboid;
import dev.frankheijden.insights.api.reflection.RChunk;
import dev.frankheijden.insights.api.reflection.RCraftChunk;
import net.minecraft.world.level.chunk.ChunkSection;
import org.bukkit.Chunk;

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
    public ChunkSection[] getChunkSections() {
        var nmsChunk = RCraftChunk.getReflection().invoke(chunk, "getHandle");
        return RChunk.getReflection().invoke(nmsChunk, "getSections");
    }
}
