package dev.frankheijden.insights.commands;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.addons.Region;
import dev.frankheijden.insights.api.commands.InsightsCommand;
import dev.frankheijden.insights.api.concurrent.ScanOptions;
import dev.frankheijden.insights.api.config.Messages;
import dev.frankheijden.insights.api.config.limits.Limit;
import dev.frankheijden.insights.api.objects.chunk.ChunkPart;
import dev.frankheijden.insights.api.objects.wrappers.ScanObject;
import dev.frankheijden.insights.api.reflection.RTileEntityTypes;
import dev.frankheijden.insights.api.tasks.ScanTask;
import dev.frankheijden.insights.api.utils.Constants;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Flag;
import org.incendo.cloud.annotations.Permission;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Command("scanregion")
public class CommandScanRegion extends InsightsCommand {

    public CommandScanRegion(InsightsPlugin plugin) {
        super(plugin);
    }

    @Command("tile")
    @Permission("insights.scanregion.tile")
    private void handleTileScan(
            Player player,
            @Flag(value = "group-by-chunk", aliases = { "c" }) boolean groupByChunk
    ) {
        handleScan(player, RTileEntityTypes.getTileEntities(), ScanOptions.materialsOnly(), false, groupByChunk);
    }

    @Command("entity")
    @Permission("insights.scanregion.entity")
    private void handleEntityScan(
            Player player,
            @Flag(value = "group-by-chunk", aliases = { "c" }) boolean groupByChunk
    ) {
        handleScan(player, Constants.SCAN_ENTITIES, ScanOptions.entitiesOnly(), false, groupByChunk);
    }

    @Command("all")
    @Permission("insights.scanregion.all")
    private void handleAllScan(
            Player player,
            @Flag(value = "group-by-chunk", aliases = { "c" }) boolean groupByChunk
    ) {
        handleScan(player, null, ScanOptions.scanOnly(), false, groupByChunk);
    }

    @Command("custom <items>")
    @Permission("insights.scanregion.custom")
    private void handleCustomScan(
            Player player,
            @Flag(value = "group-by-chunk", aliases = { "c" }) boolean groupByChunk,
            @Argument("items") ScanObject<?>[] items
    ) {
        handleScan(player, new HashSet<>(Arrays.asList(items)), ScanOptions.scanOnly(), true, groupByChunk);
    }

    @Command("limit <limit>")
    @Permission("insights.scanregion.limit")
    private void handleLimitScan(
            Player player,
            @Flag(value = "group-by-chunk", aliases = { "c" }) boolean groupByChunk,
            @Argument("limit") Limit limit
    ) {
        handleScan(player, limit.getScanObjects(), limit.getScanOptions(), false, groupByChunk);
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
            plugin.getMessages().getMessage(Messages.Key.SCANREGION_NO_REGION).sendTo(player);
            return;
        }

        List<ChunkPart> parts = optionalRegion.get().toChunkParts();
        if (groupByChunk) {
            ScanTask.scanAndDisplayGroupedByChunk(plugin, player, parts, parts.size(), options, items, false);
        } else {
            ScanTask.scanAndDisplay(plugin, player, parts, parts.size(), options, items, displayZeros);
        }
    }
}
