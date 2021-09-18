package dev.frankheijden.insights.api.concurrent.containers;

import dev.frankheijden.insights.api.concurrent.ScanOptions;
import dev.frankheijden.insights.api.objects.chunk.ChunkCuboid;
import io.papermc.lib.PaperLib;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;

public class UnloadedChunkContainer extends ChunkContainer {

    /**
     * Constructs a new UnloadedChunkContainer, for scanning of an unloaded chunk.
     */
    public UnloadedChunkContainer(World world, int chunkX, int chunkZ, ChunkCuboid cuboid, ScanOptions options) {
        super(world, chunkX, chunkZ, cuboid, options);
    }

    @Override
    public LevelChunkSection[] getChunkSections() throws Throwable {
        var serverLevel = ((CraftWorld) world).getHandle();

        int sectionsCount = serverLevel.getSectionsCount();
        var chunkSections = new LevelChunkSection[sectionsCount];

        var chunkMap = serverLevel.getChunkSource().chunkMap;
        var chunkPos = new ChunkPos(chunkX, chunkZ);

        final CompoundTag tag;
        if (PaperLib.isPaper()) {
            tag = chunkMap.regionFileCache.read(chunkPos);
        } else {
            tag = chunkMap.read(chunkPos);
        }
        if (tag == null) return chunkSections;

        CompoundTag levelTag = tag.getCompound("Level");
        ListTag sectionsTagList = levelTag.getList("Sections", 10);

        for (var i = 0; i < sectionsTagList.size(); i++) {
            CompoundTag sectionTag = sectionsTagList.getCompound(i);
            var chunkSectionPart = sectionTag.getByte("Y");

            if (sectionTag.contains("Palette", 9) && sectionTag.contains("BlockStates", 12)) {
                var chunkSection = new LevelChunkSection(chunkSectionPart);
                chunkSection.getStates().read(
                        sectionTag.getList("Palette", 10),
                        sectionTag.getLongArray("BlockStates")
                );
                chunkSections[serverLevel.getSectionIndexFromSectionY(chunkSectionPart)] = chunkSection;
            }
        }

        return chunkSections;
    }
}
