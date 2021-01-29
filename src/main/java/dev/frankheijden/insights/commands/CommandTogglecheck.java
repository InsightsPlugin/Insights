package dev.frankheijden.insights.commands;

import dev.frankheijden.insights.Insights;
import dev.frankheijden.insights.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class CommandTogglecheck implements CommandExecutor, TabExecutor {

    private static final Insights plugin = Insights.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("insights.togglecheck")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                plugin.getSqLite().toggleRealtimeCheck(player.getUniqueId());

                if (plugin.getSqLite().hasRealtimeCheckEnabled(player)) {
                    MessageUtils.sendMessage(player, "messages.togglecheck.enabled", "%name%", player.getPlayerListName());
                } else {
                    MessageUtils.sendMessage(player, "messages.togglecheck.disabled", "%name%", player.getPlayerListName());
                }
            } else {
                sender.sendMessage("This command cannot be executed from console!");
            }
        } else {
            MessageUtils.sendMessage(sender, "messages.no_permission");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return Collections.emptyList();
    }
}
