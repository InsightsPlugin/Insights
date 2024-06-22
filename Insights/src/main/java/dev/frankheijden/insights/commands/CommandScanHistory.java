package dev.frankheijden.insights.commands;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.commands.InsightsCommand;
import dev.frankheijden.insights.api.config.Messages;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;

public class CommandScanHistory extends InsightsCommand {

    public CommandScanHistory(InsightsPlugin plugin) {
        super(plugin);
    }

    @Command("scanhistory <page>")
    private void handleScanHistory(
            Player player,
            @Argument("page") Page page
    ) {
        UUID uuid = player.getUniqueId();
        Optional<Messages.PaginatedMessage<?>> historyOptional = plugin.getScanHistory().getHistory(uuid);
        if (historyOptional.isPresent()) {
            historyOptional.get().sendTo(player, page.page);
        } else {
            plugin.getMessages().getMessage(Messages.Key.SCANHISTORY_NO_HISTORY).sendTo(player);
        }
    }

    public record Page(int page) {}
}
