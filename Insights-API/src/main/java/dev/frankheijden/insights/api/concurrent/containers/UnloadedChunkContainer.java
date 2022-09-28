package dev.frankheijden.insights.api.concurrent.containers;

import ca.spottedleaf.concurrentutil.executor.standard.PrioritisedExecutor;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.concurrent.ScanOptions;
import dev.frankheijden.insights.api.objects.chunk.ChunkCuboid;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Logger;
import io.papermc.paper.chunk.system.io.RegionFileIOThread;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.entity.EntityType;

public class UnloadedChunkContainer extends ChunkContainer {

    private static final String CHUNK_ERROR = "Recoverable errors when loading section [%d, %d, %d]: %s";
    private final Logger logger = InsightsPlugin.getInstance().getLogger();

    /**
     * Constructs a new UnloadedChunkContainer, for scanning of an unloaded chunk.
     */
    public UnloadedChunkContainer(World world, int chunkX, int chunkZ, ChunkCuboid cuboid, ScanOptions options) {
        super(world, chunkX, chunkZ, cuboid, options);
    }

    @Override
    @SuppressWarnings("deprecation")
    public LevelChunkSection[] getChunkSections() {
        var serverLevel = ((CraftWorld) world).getHandle();

        int sectionsCount = serverLevel.getSectionsCount();
        var chunkSections = new LevelChunkSection[sectionsCount];
        var chunkMap = serverLevel.getChunkSource().chunkMap;
        var chunkPos = new ChunkPos(chunkX, chunkZ);

        Optional<CompoundTag> tagOptional = chunkMap.read(chunkPos).join();
        if (tagOptional.isEmpty()) return chunkSections;
        CompoundTag tag = tagOptional.get();

        ListTag sectionsTagList = tag.getList("sections", Tag.TAG_COMPOUND);

        DataResult<PalettedContainer<BlockState>> dataResult;
        for (var i = 0; i < sectionsTagList.size(); i++) {
            CompoundTag sectionTag = sectionsTagList.getCompound(i);
            var chunkSectionPart = sectionTag.getByte("Y");
            var sectionIndex = serverLevel.getSectionIndexFromSectionY(chunkSectionPart);
            if (sectionIndex < 0 || sectionIndex >= chunkSections.length) continue;

            PalettedContainer<BlockState> blockStateContainer;
            if (sectionTag.contains("block_states", Tag.TAG_COMPOUND)) {
                Codec<PalettedContainer<BlockState>> blockStateCodec = ChunkSerializer.BLOCK_STATE_CODEC;
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
                blockStateContainer = dataResult.getOrThrow(false, logger::severe);
            } else {
                blockStateContainer = new PalettedContainer<>(
                        Block.BLOCK_STATE_REGISTRY,
                        Blocks.AIR.defaultBlockState(),
                        PalettedContainer.Strategy.SECTION_STATES
                );
            }

            LevelChunkSection chunkSection = new LevelChunkSection(chunkSectionPart, blockStateContainer, null);
            chunkSections[sectionIndex] = chunkSection;
        }

        return chunkSections;
    }

    @Override
    public void getChunkEntities(Consumer<ChunkEntity> entityConsumer) throws IOException {
        var serverLevel = ((CraftWorld) world).getHandle();
        CompoundTag tag = RegionFileIOThread.loadData(
                serverLevel,
                chunkX,
                chunkZ,
                RegionFileIOThread.RegionFileType.ENTITY_DATA,
                PrioritisedExecutor.Priority.BLOCKING
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
}
