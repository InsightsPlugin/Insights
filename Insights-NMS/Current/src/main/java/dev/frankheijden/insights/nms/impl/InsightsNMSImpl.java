package dev.frankheijden.insights.nms.impl;

import dev.frankheijden.insights.nms.core.ChunkEntity;
import dev.frankheijden.insights.nms.core.ChunkSection;
import dev.frankheijden.insights.nms.core.InsightsNMS;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftChunk;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class InsightsNMSImpl extends InsightsNMS {

    @Override
    public void getLoadedChunkSections(Chunk chunk, Consumer<ChunkSection> sectionConsumer) {
        LevelChunkSection[] levelChunkSections = ((CraftChunk) chunk).getHandle(ChunkStatus.FULL).getSections();
        for (int i = 0; i < levelChunkSections.length; i++) {
            sectionConsumer.accept(new ChunkSectionImpl(levelChunkSections[i], i));
        }
    }

    @Override
    public void getUnloadedChunkSections(
            World world,
            int chunkX,
            int chunkZ,
            Consumer<ChunkSection> sectionConsumer
    ) throws IOException {
        // Use getChunkAtAsync to safely load the chunk without touching files directly
        // This prevents data corruption and uses Bukkit's proper chunk loading mechanism
        // The chunk will be loaded temporarily and then unloaded automatically
        try {
            Chunk chunk = world.getChunkAtAsync(chunkX, chunkZ, false).join();
            if (chunk != null) {
                // Chunk loaded successfully, use the same logic as loaded chunks
                getLoadedChunkSections(chunk, sectionConsumer);
            } else {
                // Chunk doesn't exist or couldn't be loaded
                // Return null sections for all expected sections
                var serverLevel = ((CraftWorld) world).getHandle();
                int sectionsCount = serverLevel.getSectionsCount();
                for (int i = 0; i < sectionsCount; i++) {
                    sectionConsumer.accept(null);
                }
            }
        } catch (Exception e) {
            throw new IOException("Failed to load chunk at (" + chunkX + ", " + chunkZ + ")", e);
        }
    }

    @Override
    public void getLoadedChunkEntities(Chunk chunk, Consumer<ChunkEntity> entityConsumer) {
        for (org.bukkit.entity.Entity bukkitEntity : chunk.getEntities()) {
            Entity entity = ((CraftEntity) bukkitEntity).getHandle();
            entityConsumer.accept(new ChunkEntity(
                    bukkitEntity.getType(),
                    entity.getBlockX(),
                    entity.getBlockY(),
                    entity.getBlockZ()
            ));
        }
    }

    @Override
    public void getUnloadedChunkEntities(
            World world,
            int chunkX,
            int chunkZ,
            Consumer<ChunkEntity> entityConsumer
    ) throws IOException {
        // Use getChunkAtAsync to safely load the chunk without touching files directly
        try {
            Chunk chunk = world.getChunkAtAsync(chunkX, chunkZ, false).join();
            if (chunk != null) {
                // Chunk loaded successfully, use the same logic as loaded chunks
                getLoadedChunkEntities(chunk, entityConsumer);
            }
            // If chunk is null, no entities to return
        } catch (Exception e) {
            throw new IOException("Failed to load chunk entities at (" + chunkX + ", " + chunkZ + ")", e);
        }
    }

    public static class ChunkSectionImpl implements ChunkSection {

        private final LevelChunkSection chunkSection;
        private final int index;

        public ChunkSectionImpl(LevelChunkSection chunkSection, int index) {
            this.chunkSection = chunkSection;
            this.index = index;
        }

        @Override
        public int index() {
            return index;
        }

        @Override
        public boolean isNull() {
            return chunkSection == null;
        }

        @Override
        public Material blockAt(int x, int y, int z) {
            return CraftMagicNumbers.getMaterial(chunkSection.getBlockState(x, y, z).getBlock());
        }

        @Override
        public void countBlocks(BiConsumer<Material, Integer> consumer) {
            chunkSection.getStates().count((state, count) -> {
                consumer.accept(CraftMagicNumbers.getMaterial(state.getBlock()), count);
            });
        }
    }
}
