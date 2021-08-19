package dev.frankheijden.insights.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.addons.Region;
import dev.frankheijden.insights.api.commands.InsightsCommand;
import dev.frankheijden.insights.api.concurrent.storage.Storage;
import dev.frankheijden.insights.api.config.Messages;
import dev.frankheijden.insights.api.objects.wrappers.ScanObject;
import dev.frankheijden.insights.api.reflection.RTileEntityTypes;
import dev.frankheijden.insights.api.utils.ChunkUtils;
import dev.frankheijden.insights.api.utils.Constants;
import dev.frankheijden.insights.api.utils.EnumUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class CommandScanCache extends InsightsCommand {

    public CommandScanCache(InsightsPlugin plugin) {
        super(plugin);
    }

    @CommandMethod("scancache tile")
    @CommandPermission("insights.scancache.tile")
    private void handleTileScan(Player player) {
        handleScan(player, RTileEntityTypes.getTileEntities(), false);
    }

    @CommandMethod("scancache entity")
    @CommandPermission("insights.scancache.entity")
    private void handleEntityScan(Player player) {
        handleScan(player, Constants.SCAN_ENTITIES, false);
    }

    @CommandMethod("scancache all")
    @CommandPermission("insights.scancache.all")
    private void handleAllScan(Player player) {
        handleScan(player, null, false);
    }

    @CommandMethod("scancache custom <items>")
    @CommandPermission("insights.scancache.custom")
    private void handleCustomScan(Player player, @Argument("items") ScanObject<?>[] items) {
        handleScan(player, new HashSet<>(Arrays.asList(items)), true);
    }

    @CommandMethod("scancache clear")
    @CommandPermission("insights.scancache.clear")
    private void handleCacheClear(Player player) {
        Location loc = player.getLocation();
        Optional<Region> optionalRegion = plugin.getAddonManager().getRegion(loc);

        // If a region is present, try to delete cache of the region.
        if (optionalRegion.isPresent()) {
            plugin.getAddonStorage().remove(optionalRegion.get().getKey());
        } else {
            plugin.getWorldStorage().getWorld(loc.getWorld().getUID()).remove(ChunkUtils.getKey(loc.getChunk()));
        }

        String areaName = optionalRegion
                .map(r -> plugin.getAddonManager().getAddon(r.getAddon()).getAreaName())
                .orElse("chunk");
        plugin.getMessages().getMessage(Messages.Key.SCANCACHE_CLEARED)
                .replace("area", areaName)
                .color()
                .sendTo(player);
    }

    /**
     * Checks the player's location for a cache and displays the distribution of items.
     */
    public void handleScan(Player player, Set<? extends ScanObject<?>> items, boolean displayZeros) {
        Location loc = player.getLocation();
        Optional<Region> optionalRegion = plugin.getAddonManager().getRegion(loc);
        Optional<Storage> optionalStorage;

        // If a region is present, try to fetch cache of the region.
        if (optionalRegion.isPresent()) {
            optionalStorage = plugin.getAddonStorage().get(optionalRegion.get().getKey());
        } else {
            optionalStorage = plugin.getWorldStorage()
                    .getWorld(loc.getWorld().getUID())
                    .get(ChunkUtils.getKey(loc.getChunk()));
        }

        if (optionalStorage.isPresent()) {
            var storage = optionalStorage.get();
            var messages = plugin.getMessages();

            // Check which items we need to display & sort them based on their name.
            ScanObject<?>[] displayItems = (items == null ? storage.keys() : items).stream()
                    .filter(item -> storage.count(item) != 0 || displayZeros)
                    .sorted(Comparator.comparing(ScanObject::name))
                    .toArray(ScanObject[]::new);

            var footer = messages.getMessage(Messages.Key.SCANCACHE_RESULT_FOOTER).replace(
                    "area", optionalRegion.map(r -> plugin.getAddonManager().getAddon(r.getAddon()).getAreaName())
                            .orElse("chunk")
            );

            var message = messages.createPaginatedMessage(
                    messages.getMessage(Messages.Key.SCANCACHE_RESULT_HEADER),
                    Messages.Key.SCANCACHE_RESULT_FORMAT,
                    footer,
                    displayItems,
                    storage::count,
                    item -> EnumUtils.pretty(item.getObject())
            );

            plugin.getScanHistory().setHistory(player.getUniqueId(), message);
            message.sendTo(player, 0);
        } else {
            plugin.getMessages().getMessage(Messages.Key.SCANCACHE_NO_CACHE)
                    .color()
                    .sendTo(player);
        }
    }
}
