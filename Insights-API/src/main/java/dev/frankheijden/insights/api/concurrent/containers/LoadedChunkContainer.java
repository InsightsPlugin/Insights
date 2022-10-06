package dev.frankheijden.insights.api.concurrent.containers;

import dev.frankheijden.insights.api.concurrent.ScanOptions;
import dev.frankheijden.insights.api.objects.chunk.ChunkCuboid;
import dev.frankheijden.insights.nms.core.ChunkEntity;
import dev.frankheijden.insights.nms.core.ChunkSection;
import dev.frankheijden.insights.nms.core.InsightsNMS;
import org.bukkit.Chunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.function.Consumer;

public class LoadedChunkContainer extends ChunkContainer {

    private final Chunk chunk;

    /**
     * Constructs a new LoadedChunkContainer, for scanning of a loaded chunk.
     */
    public LoadedChunkContainer(InsightsNMS nms, Chunk chunk, ChunkCuboid cuboid, ScanOptions options) {
        super(nms, chunk.getWorld(), chunk.getX(), chunk.getZ(), cuboid, options);
        this.chunk = chunk;
    }

    @Override
    public void getChunkSections(Consumer<@Nullable ChunkSection> sectionConsumer) {
        nms.getLoadedChunkSections(chunk, sectionConsumer);
    }

    @Override
    public void getChunkEntities(Consumer<@NotNull ChunkEntity> entityConsumer) {
        nms.getLoadedChunkEntities(chunk, entityConsumer);
    }
}
