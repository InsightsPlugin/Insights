package net.frankheijden.blocklimiter.commands;

import net.frankheijden.blocklimiter.BlockLimiter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class CommandTogglecheck implements CommandExecutor, TabExecutor {
    private BlockLimiter plugin;

    public CommandTogglecheck(BlockLimiter plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("blocklimiter.togglecheck")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                plugin.sqLite.toggleRealtimeCheck(player);

                boolean hasRealtimeCheckEnabled = plugin.sqLite.hasRealtimeCheckEnabled(player);
                if (hasRealtimeCheckEnabled) {
                    plugin.utils.sendMessage(player, "messages.togglecheck.enabled", "%name%", player.getPlayerListName());
                } else {
                    plugin.utils.sendMessage(player, "messages.togglecheck.disabled", "%name%", player.getPlayerListName());
                }
            } else {
                sender.sendMessage("This command cannot be executed from console!");
            }
        } else {
            plugin.utils.sendMessage(sender, "messages.no_permission");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return Collections.emptyList();
    }
}
