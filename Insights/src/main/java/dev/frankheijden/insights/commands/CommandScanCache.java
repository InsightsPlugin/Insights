package dev.frankheijden.insights.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.region.Region;
import dev.frankheijden.insights.api.commands.InsightsCommand;
import dev.frankheijden.insights.api.concurrent.storage.Storage;
import dev.frankheijden.insights.api.config.Messages;
import dev.frankheijden.insights.api.config.limits.Limit;
import dev.frankheijden.insights.api.objects.wrappers.ScanObject;
import dev.frankheijden.insights.api.reflection.RTileEntityTypes;
import dev.frankheijden.insights.api.utils.Constants;
import dev.frankheijden.insights.api.utils.EnumUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

@CommandMethod("scancache <region>")
public class CommandScanCache extends InsightsCommand {

    public CommandScanCache(InsightsPlugin plugin) {
        super(plugin);
    }

    @CommandMethod("tile")
    @CommandPermission("insights.scancache.tile")
    private void handleTileScan(Player player, @Argument("region") Region region) {
        handleScan(player, region, RTileEntityTypes.getTileEntities(), false);
    }

    @CommandMethod("entity")
    @CommandPermission("insights.scancache.entity")
    private void handleEntityScan(Player player, @Argument("region") Region region) {
        handleScan(player, region, Constants.SCAN_ENTITIES, false);
    }

    @CommandMethod("all")
    @CommandPermission("insights.scancache.all")
    private void handleAllScan(Player player, @Argument("region") Region region) {
        handleScan(player, region, null, false);
    }

    @CommandMethod("custom <items>")
    @CommandPermission("insights.scancache.custom")
    private void handleCustomScan(
            Player player,
            @Argument("region") Region region,
            @Argument("items") ScanObject<?>[] items
    ) {
        handleScan(player, region, new HashSet<>(Arrays.asList(items)), true);
    }

    @CommandMethod("limit <limit>")
    @CommandPermission("insights.scancache.limit")
    private void handleLimitScan(
            Player player,
            @Argument("region") Region region,
            @Argument("limit") Limit limit
    ) {
        handleScan(player, region, limit.scanObjects(), false);
    }

    @CommandMethod("clear")
    @CommandPermission("insights.scancache.clear")
    private void handleCacheClear(Player player, @Argument("region") Region region) {
        var regionManager = plugin.regionManager();
        regionManager.regionStorage().remove(region);

        plugin.messages()
                .getMessage(Messages.Key.SCANCACHE_CLEARED)
                .addTemplates(
                        Messages.tagOf(
                                "area",
                                regionManager.areaName(region)
                        )
                )
                .sendTo(player);
    }

    /**
     * Checks the player's location for a cache and displays the distribution of items.
     */
    public void handleScan(Player player, Region region, Set<? extends ScanObject<?>> items, boolean displayZeros) {
        Location loc = player.getLocation();
        var regionManager = plugin.regionManager();
        Storage storage = regionManager.regionStorage().get(region);

        var messages = plugin.messages();
        if (storage != null) {
            // Check which items we need to display & sort them based on their name.
            ScanObject<?>[] displayItems = (items == null ? storage.keys() : items).stream()
                    .filter(item -> storage.count(item) != 0 || displayZeros)
                    .sorted(Comparator.comparing(ScanObject::name))
                    .toArray(ScanObject[]::new);

            var footer = messages
                    .getMessage(Messages.Key.SCANCACHE_RESULT_FOOTER)
                    .addTemplates(
                            Messages.tagOf(
                                    "area",
                                    regionManager.areaName(region)
                            )
                    );

            var message = messages.createPaginatedMessage(
                    messages.getMessage(Messages.Key.SCANCACHE_RESULT_HEADER),
                    Messages.Key.SCANCACHE_RESULT_FORMAT,
                    footer,
                    displayItems,
                    storage::count,
                    item -> Component.text(EnumUtils.pretty(item.getObject()))
            );

            plugin.scanHistory().setHistory(player.getUniqueId(), message);
            message.sendTo(player, 0);
        } else {
            messages.getMessage(Messages.Key.SCANCACHE_NO_CACHE).sendTo(player);
        }
    }
}
