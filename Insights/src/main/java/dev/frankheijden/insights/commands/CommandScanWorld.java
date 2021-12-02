package dev.frankheijden.insights.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.Flag;
import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.commands.InsightsCommand;
import dev.frankheijden.insights.api.concurrent.ScanOptions;
import dev.frankheijden.insights.api.objects.chunk.ChunkLocation;
import dev.frankheijden.insights.api.objects.chunk.ChunkPart;
import dev.frankheijden.insights.api.objects.wrappers.ScanObject;
import dev.frankheijden.insights.api.reflection.RTileEntityTypes;
import dev.frankheijden.insights.api.tasks.ScanTask;
import dev.frankheijden.insights.api.utils.Constants;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@CommandMethod("scanworld")
public class CommandScanWorld extends InsightsCommand {

    public CommandScanWorld(InsightsPlugin plugin) {
        super(plugin);
    }

    @CommandMethod("tile")
    @CommandPermission("insights.scanworld.tile")
    private void handleTileScan(
            Player player,
            @Flag(value = "group-by-chunk", aliases = { "c" }) boolean groupByChunk
    ) {
        handleScan(player, RTileEntityTypes.getTileEntities(), ScanOptions.materialsOnly(), false, groupByChunk);
    }

    @CommandMethod("entity")
    @CommandPermission("insights.scanworld.entity")
    private void handleEntityScan(
            Player player,
            @Flag(value = "group-by-chunk", aliases = { "c" }) boolean groupByChunk
    ) {
        handleScan(player, Constants.SCAN_ENTITIES, ScanOptions.entitiesOnly(), false, groupByChunk);
    }

    @CommandMethod("all")
    @CommandPermission("insights.scanworld.all")
    private void handleAllScan(
            Player player,
            @Flag(value = "group-by-chunk", aliases = { "c" }) boolean groupByChunk
    ) {
        handleScan(player, null, ScanOptions.scanOnly(), false, groupByChunk);
    }

    @CommandMethod("custom <items>")
    @CommandPermission("insights.scanworld.custom")
    private void handleCustomScan(
            Player player,
            @Flag(value = "group-by-chunk", aliases = { "c" }) boolean groupByChunk,
            @Argument("items") ScanObject<?>[] items
    ) {
        handleScan(player, new HashSet<>(Arrays.asList(items)), ScanOptions.scanOnly(), true, groupByChunk);
    }

    /**
     * Scans chunks in the world of a player.
     */
    public void handleScan(
            Player player,
            Set<? extends ScanObject<?>> items,
            ScanOptions options,
            boolean displayZeros,
            boolean groupByChunk
    ) {
        World world = player.getWorld();

        // Generate chunk parts
        Chunk[] chunks = world.getLoadedChunks();
        List<ChunkPart> chunkParts = new ArrayList<>(chunks.length);
        for (Chunk chunk : chunks) {
            chunkParts.add(ChunkLocation.of(chunk).toPart());
        }

        if (groupByChunk) {
            ScanTask.scanAndDisplayGroupedByChunk(plugin, player, chunkParts, chunkParts.size(), options, items, false);
        } else {
            ScanTask.scanAndDisplay(plugin, player, chunkParts, chunkParts.size(), options, items, displayZeros);
        }
    }
}
