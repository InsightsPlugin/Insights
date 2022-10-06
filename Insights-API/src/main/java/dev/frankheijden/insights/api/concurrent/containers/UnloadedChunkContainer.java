package dev.frankheijden.insights.api.concurrent.containers;

import dev.frankheijden.insights.api.concurrent.ScanOptions;
import dev.frankheijden.insights.api.objects.chunk.ChunkCuboid;
import dev.frankheijden.insights.nms.core.ChunkEntity;
import dev.frankheijden.insights.nms.core.ChunkSection;
import dev.frankheijden.insights.nms.core.InsightsNMS;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.IOException;
import java.util.function.Consumer;

public class UnloadedChunkContainer extends ChunkContainer {

    /**
     * Constructs a new UnloadedChunkContainer, for scanning of an unloaded chunk.
     */
    public UnloadedChunkContainer(
            InsightsNMS nms,
            World world,
            int chunkX,
            int chunkZ,
            ChunkCuboid cuboid,
            ScanOptions options
    ) {
        super(nms, world, chunkX, chunkZ, cuboid, options);
    }

    @Override
    public void getChunkSections(Consumer<@Nullable ChunkSection> sectionConsumer) {
        nms.getUnloadedChunkSections(world, chunkX, chunkZ, sectionConsumer);
    }

    @Override
    public void getChunkEntities(Consumer<@NotNull ChunkEntity> entityConsumer) throws IOException {
        nms.getUnloadedChunkEntities(world, chunkX, chunkZ, entityConsumer);
    }
}
