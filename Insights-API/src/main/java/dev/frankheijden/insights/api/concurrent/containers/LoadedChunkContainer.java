package dev.frankheijden.insights.api.concurrent.containers;

import dev.frankheijden.insights.api.concurrent.ScanOptions;
import dev.frankheijden.insights.api.objects.chunk.ChunkCuboid;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.bukkit.Chunk;
import org.bukkit.craftbukkit.v1_19_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import java.util.function.Consumer;

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

    @Override
    public void getChunkEntities(Consumer<ChunkEntity> entityConsumer) {
        for (Entity entity : chunk.getEntities()) {
            net.minecraft.world.entity.Entity e = ((CraftEntity) entity).getHandle();
            entityConsumer.accept(new ChunkEntity(entity.getType(), e.getBlockX(), e.getBlockY(), e.getBlockZ()));
        }
    }
}
