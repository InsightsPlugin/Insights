package net.frankheijden.insights.commands;

import net.frankheijden.insights.Insights;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandInsights implements CommandExecutor, TabExecutor {
    private Insights plugin;

    public CommandInsights(Insights plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(new String[]{
                    plugin.utils.color("&8&l&m---------------=&r&8[ &b&lInsights&8 ]&l&m=----------------"),
                    plugin.utils.color("&b Plugin version: &7" + plugin.getDescription().getVersion()),
                    plugin.utils.color("&b Plugin author: &7https://www.spigotmc.org/members/213966/"),
                    plugin.utils.color("&b Plugin link: &7https://www.spigotmc.org/resources/56489/"),
                    plugin.utils.color("&8&m-------------------------------------------------")
            });
            return true;
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("insights.reload")) {
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


    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>(Collections.singletonList("help"));
            if (sender.hasPermission("insights.reload")) {
                list.add("reload");
            }
            return StringUtil.copyPartialMatches(args[0], list, new ArrayList<>());
        }
        return Collections.emptyList();
    }
}
