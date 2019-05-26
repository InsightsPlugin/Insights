package net.frankheijden.blocklimiter.commands;

import net.frankheijden.blocklimiter.BlockLimiter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandBlockLimiter implements CommandExecutor {
    private BlockLimiter plugin;

    public CommandBlockLimiter(BlockLimiter plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(new String[]{
                    plugin.utils.color("&8&l&m--------------=&r&8[ &b&lBlockLimiter&8 ]&l&m=--------------"),
                    plugin.utils.color("&b Plugin version: &7" + plugin.getDescription().getVersion()),
                    plugin.utils.color("&b Plugin author: &7https://www.spigotmc.org/members/213966/"),
                    plugin.utils.color("&b Plugin link: &7https://www.spigotmc.org/resources/56489/"),
                    plugin.utils.color("&8&m-------------------------------------------------")
            });
            return true;
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("blocklimiter.reload")) {
                    try {
                        plugin.utils.reload();
                    } catch (Exception ex) {
                        plugin.utils.sendMessage(sender, "messages.reload_failed");
                        return true;
                    }

                    plugin.utils.sendMessage(sender, "messages.reload");
                } else {
                    plugin.utils.sendMessage(sender, "messages.no_permission");
                }
                return true;
            }
        }

        plugin.utils.sendMessage(sender, "messages.help");
        return true;
    }
}
