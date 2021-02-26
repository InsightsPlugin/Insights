package dev.frankheijden.insights.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.addons.Region;
import dev.frankheijden.insights.api.commands.InsightsCommand;
import dev.frankheijden.insights.api.concurrent.storage.DistributionStorage;
import dev.frankheijden.insights.api.config.Messages;
import dev.frankheijden.insights.api.objects.wrappers.ScanObject;
import dev.frankheijden.insights.api.reflection.RTileEntityTypes;
import dev.frankheijden.insights.api.utils.ChunkUtils;
import dev.frankheijden.insights.api.utils.EnumUtils;
import dev.frankheijden.insights.api.utils.StringUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.Arrays;
import java.util.Collection;
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
        Optional<DistributionStorage> optionalStorage;

        // If a region is present, try to fetch cache of the region.
        if (optionalRegion.isPresent()) {
            optionalStorage = plugin.getAddonStorage().get(optionalRegion.get().getKey());
        } else {
            optionalStorage = plugin.getWorldStorage()
                    .getWorld(loc.getWorld().getUID())
                    .get(ChunkUtils.getKey(loc.getChunk()));
        }

        if (optionalStorage.isPresent()) {
            DistributionStorage storage = optionalStorage.get();

            Messages messages = plugin.getMessages();
            messages.getMessage(Messages.Key.SCANCACHE_RESULT_HEADER).color().sendTo(player);

            Collection<? extends ScanObject<?>> displayItems = items == null ? storage.keys() : items;
            for (ScanObject<?> item : displayItems) {
                int count = storage.count(item);
                if (count == 0 && !displayZeros) continue;

                messages.getMessage(Messages.Key.SCANCACHE_RESULT_FORMAT)
                        .replace(
                                "entry", EnumUtils.pretty(item.getObject()),
                                "count", StringUtils.pretty(count)
                        )
                        .color()
                        .sendTo(player);
            }

            String areaName = optionalRegion
                    .map(r -> plugin.getAddonManager().getAddon(r.getAddon()).getAreaName())
                    .orElse("chunk");
            messages.getMessage(Messages.Key.SCANCACHE_RESULT_FOOTER)
                    .replace("area", areaName)
                    .color()
                    .sendTo(player);
        } else {
            plugin.getMessages().getMessage(Messages.Key.SCANCACHE_NO_CACHE)
                    .color()
                    .sendTo(player);
        }
    }
}
