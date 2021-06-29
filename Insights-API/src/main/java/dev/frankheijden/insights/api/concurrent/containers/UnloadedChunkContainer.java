package dev.frankheijden.insights.api.concurrent.containers;

import dev.frankheijden.insights.api.concurrent.ScanOptions;
import dev.frankheijden.insights.api.objects.chunk.ChunkCuboid;
import dev.frankheijden.insights.api.reflection.RCraftWorld;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.chunk.ChunkSection;
import org.bukkit.World;

public class UnloadedChunkContainer extends ChunkContainer {

    /**
     * Constructs a new UnloadedChunkContainer, for scanning of an unloaded chunk.
     */
    public UnloadedChunkContainer(World world, int chunkX, int chunkZ, ChunkCuboid cuboid, ScanOptions options) {
        super(world, chunkX, chunkZ, cuboid, options);
    }

    @Override
    public ChunkSection[] getChunkSections() throws Throwable {
        WorldServer serverLevel = RCraftWorld.getServerLevel(world);

        int sectionsCount = serverLevel.getSectionsCount();
        var chunkSections = new ChunkSection[sectionsCount];

        NBTTagCompound nbt = serverLevel.getChunkProvider().a.read(new ChunkCoordIntPair(chunkX, chunkZ));
        if (nbt == null) return chunkSections;

        NBTTagCompound levelNbt = nbt.getCompound("Level");
        NBTTagList sectionsNbtList = levelNbt.getList("Sections", 10);

        for (var i = 0; i < sectionsNbtList.size(); i++) {
            NBTTagCompound sectionNbt = sectionsNbtList.getCompound(i);
            var chunkSectionPart = sectionNbt.getByte("Y");

            if (sectionNbt.hasKeyOfType("Palette", 9) && sectionNbt.hasKeyOfType("BlockStates", 12)) {
                var chunkSection = new ChunkSection(chunkSectionPart);

                chunkSection.getBlocks().a(
                        sectionNbt.getList("Palette", 10),
                        sectionNbt.getLongArray("BlockStates")
                );
                chunkSections[serverLevel.getSectionIndexFromSectionY(chunkSectionPart)] = chunkSection;
            }
        }

        return chunkSections;
    }
}
