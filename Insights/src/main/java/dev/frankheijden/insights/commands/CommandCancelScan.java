package dev.frankheijden.insights.commands;

import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.commands.InsightsCommand;
import dev.frankheijden.insights.api.config.Messages;
import dev.frankheijden.insights.api.tasks.ScanTask;
import org.bukkit.entity.Player;

public class CommandCancelScan extends InsightsCommand {

    public CommandCancelScan(InsightsPlugin plugin) {
        super(plugin);
    }

    @CommandMethod("cancelscan")
    @CommandPermission("insights.cancelscan")
    private void handleCancelScan(Player player) {
        if (ScanTask.cancelScan(player.getUniqueId())) {
            // Player will be notified of the results, no need to send verification.
        } else {
            plugin.getMessages().getMessage(Messages.Key.CANCELSCAN_NO_SCAN).sendTo(player);
        }
    }
}
