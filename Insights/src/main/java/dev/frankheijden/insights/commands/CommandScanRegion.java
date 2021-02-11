package dev.frankheijden.insights.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.addons.Region;
import dev.frankheijden.insights.api.commands.InsightsCommand;
import dev.frankheijden.insights.api.config.Messages;
import dev.frankheijden.insights.api.reflection.RTileEntityTypes;
import dev.frankheijden.insights.api.tasks.ScanTask;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class CommandScanRegion extends InsightsCommand {

    public CommandScanRegion(InsightsPlugin plugin) {
        super(plugin);
    }

    @CommandMethod("scanregion tile")
    @CommandPermission("insights.scanregion.tile")
    private void handleTileScan(Player player) {
        handleScan(player, RTileEntityTypes.getTileEntityMaterials(), false);
    }

    @CommandMethod("scanregion all")
    @CommandPermission("insights.scanregion.all")
    private void handleAllScan(Player player) {
        handleScan(player, null, false);
    }

    @CommandMethod("scanregion custom <materials>")
    @CommandPermission("insights.scanregion.custom")
    private void handleCustomScan(Player player, @Argument("materials") Material[] materials) {
        handleScan(player, new HashSet<>(Arrays.asList(materials)), true);
    }

    /**
     * Checks the player's location for a region and scans it for materials.
     */
    public void handleScan(Player player, Set<Material> materials, boolean displayZeros) {
        Optional<Region> optionalRegion = plugin.getAddonManager().getRegion(player.getLocation());
        if (!optionalRegion.isPresent()) {
            plugin.getMessages().getMessage(Messages.Key.SCANREGION_NO_REGION)
                    .color()
                    .sendTo(player);
            return;
        }

        ScanTask.scanAndDisplay(plugin, player, optionalRegion.get().toChunkParts(), materials, displayZeros);
    }
}
