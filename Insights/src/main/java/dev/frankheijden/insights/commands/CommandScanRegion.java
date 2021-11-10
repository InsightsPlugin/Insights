package dev.frankheijden.insights.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.Flag;
import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.addons.Region;
import dev.frankheijden.insights.api.commands.InsightsCommand;
import dev.frankheijden.insights.api.concurrent.ScanOptions;
import dev.frankheijden.insights.api.config.Messages;
import dev.frankheijden.insights.api.objects.chunk.ChunkPart;
import dev.frankheijden.insights.api.objects.wrappers.ScanObject;
import dev.frankheijden.insights.api.reflection.RTileEntityTypes;
import dev.frankheijden.insights.api.tasks.ScanTask;
import dev.frankheijden.insights.api.utils.Constants;
import org.bukkit.entity.Player;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@CommandMethod("scanregion")
public class CommandScanRegion extends InsightsCommand {

    public CommandScanRegion(InsightsPlugin plugin) {
        super(plugin);
    }

    @CommandMethod("tile")
    @CommandPermission("insights.scanregion.tile")
    private void handleTileScan(
            Player player,
            @Flag(value = "group-by-chunk", aliases = { "c" }) boolean groupByChunk
    ) {
        handleScan(player, RTileEntityTypes.getTileEntities(), ScanOptions.materialsOnly(), false, groupByChunk);
    }

    @CommandMethod("entity")
    @CommandPermission("insights.scanregion.entity")
    private void handleEntityScan(
            Player player,
            @Flag(value = "group-by-chunk", aliases = { "c" }) boolean groupByChunk
    ) {
        handleScan(player, Constants.SCAN_ENTITIES, ScanOptions.entitiesOnly(), false, groupByChunk);
    }

    @CommandMethod("all")
    @CommandPermission("insights.scanregion.all")
    private void handleAllScan(
            Player player,
            @Flag(value = "group-by-chunk", aliases = { "c" }) boolean groupByChunk
    ) {
        handleScan(player, null, ScanOptions.scanOnly(), false, groupByChunk);
    }

    @CommandMethod("custom <items>")
    @CommandPermission("insights.scanregion.custom")
    private void handleCustomScan(
            Player player,
            @Flag(value = "group-by-chunk", aliases = { "c" }) boolean groupByChunk,
            @Argument("items") ScanObject<?>[] items
    ) {
        handleScan(player, new HashSet<>(Arrays.asList(items)), ScanOptions.scanOnly(), true, groupByChunk);
    }

    /**
     * Checks the player's location for a region and scans it for materials.
     */
    public void handleScan(
            Player player,
            Set<? extends ScanObject<?>> items,
            ScanOptions options,
            boolean displayZeros,
            boolean groupByChunk
    ) {
        Optional<Region> optionalRegion = plugin.getAddonManager().getRegion(player.getLocation());
        if (optionalRegion.isEmpty()) {
            plugin.getMessages().getMessage(Messages.Key.SCANREGION_NO_REGION)
                    .color()
                    .sendTo(player);
            return;
        }

        List<ChunkPart> parts = optionalRegion.get().toChunkParts();
        if (groupByChunk) {
            ScanTask.scanAndDisplayGroupedByChunk(plugin, player, parts, options, items, false);
        } else {
            ScanTask.scanAndDisplay(plugin, player, parts, options, items, displayZeros);
        }
    }
}
