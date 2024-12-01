package dev.frankheijden.insights.nms.impl;

import ca.spottedleaf.concurrentutil.util.Priority;
import ca.spottedleaf.moonrise.patches.chunk_system.io.MoonriseRegionFileIO;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.frankheijden.insights.nms.core.ChunkEntity;
import dev.frankheijden.insights.nms.core.ChunkSection;
import dev.frankheijden.insights.nms.core.InsightsNMS;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.storage.SerializableChunkData;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftChunk;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.entity.EntityType;
import java.io.IOException;
import java.util.Optional;
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
    public void getUnloadedChunkSections(World world, int chunkX, int chunkZ, Consumer<ChunkSection> sectionConsumer) {
        var serverLevel = ((CraftWorld) world).getHandle();
        int sectionsCount = serverLevel.getSectionsCount();
        var chunkMap = serverLevel.getChunkSource().chunkMap;
        var chunkPos = new ChunkPos(chunkX, chunkZ);

        Optional<CompoundTag> tagOptional = chunkMap.read(chunkPos).join();
        if (tagOptional.isEmpty()) return;
        CompoundTag tag = tagOptional.get();

        ListTag sectionsTagList = tag.getList("sections", Tag.TAG_COMPOUND);

        DataResult<PalettedContainer<BlockState>> dataResult;
        int nonNullSectionCount = 0;
        for (int i = 0; i < sectionsTagList.size(); i++) {
            CompoundTag sectionTag = sectionsTagList.getCompound(i);
            var chunkSectionPart = sectionTag.getByte("Y");
            var sectionIndex = serverLevel.getSectionIndexFromSectionY(chunkSectionPart);
            if (sectionIndex < 0 || sectionIndex >= sectionsCount) continue;

            PalettedContainer<BlockState> blockStateContainer;
            if (sectionTag.contains("block_states", Tag.TAG_COMPOUND)) {
                Codec<PalettedContainer<BlockState>> blockStateCodec = SerializableChunkData.BLOCK_STATE_CODEC;
                dataResult = blockStateCodec.parse(
                        NbtOps.INSTANCE,
                        sectionTag.getCompound("block_states")
                ).promotePartial(message -> logger.severe(String.format(
                        CHUNK_ERROR,
                        chunkX,
                        chunkSectionPart,
                        chunkZ,
                        message
                )));

                try {
                    blockStateContainer = dataResult.getOrThrow();
                } catch (IllegalStateException ex) {
                    logger.severe(ex.getMessage());
                    throw ex;
                }
            } else {
                blockStateContainer = new PalettedContainer<>(
                        Block.BLOCK_STATE_REGISTRY,
                        Blocks.AIR.defaultBlockState(),
                        PalettedContainer.Strategy.SECTION_STATES,
                        null
                );
            }

            LevelChunkSection chunkSection = new LevelChunkSection(blockStateContainer, null);
            sectionConsumer.accept(new ChunkSectionImpl(chunkSection, sectionIndex));
            nonNullSectionCount++;
        }

        for (int i = nonNullSectionCount; i < sectionsCount; i++) {
            sectionConsumer.accept(null);
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
        var serverLevel = ((CraftWorld) world).getHandle();
        CompoundTag tag = MoonriseRegionFileIO.loadData(
                serverLevel,
                chunkX,
                chunkZ,
                MoonriseRegionFileIO.RegionFileType.ENTITY_DATA,
                Priority.BLOCKING
        );
        if (tag == null) return;

        readChunkEntities(tag.getList("Entities", Tag.TAG_COMPOUND), entityConsumer);
    }

    private void readChunkEntities(ListTag listTag, Consumer<ChunkEntity> entityConsumer) {
        for (Tag tag : listTag) {
            readChunkEntities((CompoundTag) tag, entityConsumer);
        }
    }

    private void readChunkEntities(CompoundTag nbt, Consumer<ChunkEntity> entityConsumer) {
        var typeOptional = net.minecraft.world.entity.EntityType.by(nbt);
        if (typeOptional.isPresent()) {
            String entityTypeName = net.minecraft.world.entity.EntityType.getKey(typeOptional.get()).getPath();
            ListTag posList = nbt.getList("Pos", Tag.TAG_DOUBLE);
            entityConsumer.accept(new ChunkEntity(
                    EntityType.fromName(entityTypeName),
                    Mth.floor(Mth.clamp(posList.getDouble(0), -3E7D, 3E7D)),
                    Mth.floor(Mth.clamp(posList.getDouble(1), -2E7D, 2E7D)),
                    Mth.floor(Mth.clamp(posList.getDouble(2), -3E7D, 3E7D))
            ));
        }

        if (nbt.contains("Passengers", Tag.TAG_LIST)) {
            readChunkEntities(nbt.getList("Passengers", Tag.TAG_COMPOUND), entityConsumer);
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
