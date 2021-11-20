package dev.frankheijden.insights.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.Flag;
import cloud.commandframework.annotations.specifier.Range;
import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.commands.InsightsCommand;
import dev.frankheijden.insights.api.concurrent.ScanOptions;
import dev.frankheijden.insights.api.objects.wrappers.ScanObject;
import dev.frankheijden.insights.api.reflection.RTileEntityTypes;
import dev.frankheijden.insights.api.tasks.ScanTask;
import dev.frankheijden.insights.api.util.LazyChunkPartRadiusIterator;
import dev.frankheijden.insights.api.utils.Constants;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@CommandMethod("scan <radius>")
public class CommandScan extends InsightsCommand {

    public CommandScan(InsightsPlugin plugin) {
        super(plugin);
    }

    @CommandMethod("tile")
    @CommandPermission("insights.scan.tile")
    private void handleTileScan(
            Player player,
            @Argument("radius") @Range(min = "0", max = "50") int radius,
            @Flag(value = "group-by-chunk", aliases = { "c" }) boolean groupByChunk
    ) {
        handleScan(
                player,
                radius,
                RTileEntityTypes.getTileEntities(),
                ScanOptions.materialsOnly(),
                false,
                groupByChunk
        );
    }

    @CommandMethod("entity")
    @CommandPermission("insights.scan.entity")
    private void handleEntityScan(
            Player player,
            @Argument("radius") @Range(min = "0", max = "50") int radius,
            @Flag(value = "group-by-chunk", aliases = { "c" }) boolean groupByChunk
    ) {
        handleScan(player, radius, Constants.SCAN_ENTITIES, ScanOptions.entitiesOnly(), false, groupByChunk);
    }

    @CommandMethod("all")
    @CommandPermission("insights.scan.all")
    private void handleAllScan(
            Player player,
            @Argument("radius") @Range(min = "0", max = "50") int radius,
            @Flag(value = "group-by-chunk", aliases = { "c" }) boolean groupByChunk
    ) {
        handleScan(player, radius, null, ScanOptions.scanOnly(), false, groupByChunk);
    }

    @CommandMethod("custom <items>")
    @CommandPermission("insights.scan.custom")
    private void handleCustomScan(
            Player player,
            @Argument("radius") @Range(min = "0", max = "50") int radius,
            @Flag(value = "group-by-chunk", aliases = { "c" }) boolean groupByChunk,
            @Argument("items") ScanObject<?>[] items
    ) {
        List<ScanObject<?>> scanObjects = Arrays.asList(items);
        boolean hasOnlyEntities = scanObjects.stream()
                .allMatch(s -> s.getType() == ScanObject.Type.ENTITY);
        boolean hasOnlyMaterials = scanObjects.stream()
                .allMatch(s -> s.getType() == ScanObject.Type.MATERIAL);

        ScanOptions options;
        if (hasOnlyEntities) {
            options = ScanOptions.entitiesOnly();
        } else if (hasOnlyMaterials) {
            options = ScanOptions.materialsOnly();
        } else {
            options = ScanOptions.scanOnly();
        }

        handleScan(player, radius, new HashSet<>(scanObjects), options, true, groupByChunk);
    }

    /**
     * Scans chunks in a radius around a player.
     */
    public void handleScan(
            Player player,
            int radius,
            Set<? extends ScanObject<?>> items,
            ScanOptions options,
            boolean displayZeros,
            boolean groupByChunk
    ) {
        Chunk chunk = player.getLocation().getChunk();
        World world = chunk.getWorld();
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();

        LazyChunkPartRadiusIterator it = new LazyChunkPartRadiusIterator(world, chunkX, chunkZ, radius);

        if (groupByChunk) {
            ScanTask.scanAndDisplayGroupedByChunk(plugin, player, it, it.getChunkCount(), options, items, false);
        } else {
            ScanTask.scanAndDisplay(plugin, player, it, it.getChunkCount(), options, items, displayZeros);
        }
    }
}
