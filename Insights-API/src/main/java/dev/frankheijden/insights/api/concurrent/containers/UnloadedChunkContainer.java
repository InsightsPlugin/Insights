package dev.frankheijden.insights.api.concurrent.containers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.concurrent.ScanOptions;
import dev.frankheijden.insights.api.objects.chunk.ChunkCuboid;
import java.io.IOException;
import java.util.logging.Logger;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;

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
    public LevelChunkSection[] getChunkSections() throws IOException {
        var serverLevel = ((CraftWorld) world).getHandle();

        int sectionsCount = serverLevel.getSectionsCount();
        var chunkSections = new LevelChunkSection[sectionsCount];

        CompoundTag tag = serverLevel.getChunkSource().chunkMap.read(new ChunkPos(chunkX, chunkZ));
        if (tag == null) return chunkSections;

        ListTag sectionsTagList = tag.getList("sections", 10);

        DataResult<PalettedContainer<BlockState>> dataResult;
        for (var i = 0; i < sectionsTagList.size(); i++) {
            CompoundTag sectionTag = sectionsTagList.getCompound(i);
            var chunkSectionPart = sectionTag.getByte("Y");

            PalettedContainer<BlockState> blockStateContainer;
            if (sectionTag.contains("block_states", 10)) {
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
            chunkSections[serverLevel.getSectionIndexFromSectionY(chunkSectionPart)] = chunkSection;
        }

        return chunkSections;
    }
}
