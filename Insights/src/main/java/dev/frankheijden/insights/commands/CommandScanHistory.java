package dev.frankheijden.insights.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.commands.InsightsCommand;
import dev.frankheijden.insights.api.config.Messages;
import java.util.Optional;
import java.util.UUID;
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
        UUID uuid = player.getUniqueId();
        Optional<Messages.PaginatedMessage<?>> historyOptional = plugin.scanHistory().getHistory(uuid);
        if (historyOptional.isPresent()) {
            historyOptional.get().sendTo(player, page.page);
        } else {
            plugin.messages().getMessage(Messages.Key.SCANHISTORY_NO_HISTORY).sendTo(player);
        }
    }

    public record Page(int page) {}
}
