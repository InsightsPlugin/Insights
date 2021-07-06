package dev.frankheijden.insights.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.commands.InsightsCommand;
import dev.frankheijden.insights.api.config.Messages;
import java.util.Optional;
import org.bukkit.entity.Player;

public class CommandScanHistory extends InsightsCommand {

    public CommandScanHistory(InsightsPlugin plugin) {
        super(plugin);
    }

    @CommandMethod("scanhistory <page>")
    @CommandPermission("insights.scanregion.tile")
    private void handleScanHistory(
            Player player,
            @Argument("page") Page page
    ) {
        Optional<Messages.PaginatedMessage> historyOptional = plugin.getScanHistory().getHistory(player.getUniqueId());
        if (historyOptional.isPresent()) {
            Messages.PaginatedMessage history = historyOptional.get();
            history.sendTo(player, page.page);
        } else {
            plugin.getMessages().getMessage(Messages.Key.SCANHISTORY_NO_HISTORY).color().sendTo(player);
        }
    }

    public record Page(int page) {}
}
