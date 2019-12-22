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
                    plugin.getUtils().color("&8&l&m---------------=&r&8[ &b&lInsights&8 ]&l&m=----------------"),
                    plugin.getUtils().color("&b Plugin version: &7" + plugin.getDescription().getVersion()),
                    plugin.getUtils().color("&b Plugin author: &7https://www.spigotmc.org/members/213966/"),
                    plugin.getUtils().color("&b Plugin link: &7https://www.spigotmc.org/resources/56489/"),
                    plugin.getUtils().color("&8&m-------------------------------------------------")
            });
            return true;
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("insights.reload")) {
                    try {
                        plugin.reload();
                    } catch (Exception ex) {
                        plugin.getUtils().sendMessage(sender, "messages.insights.reload_failed");
                        return true;
                    }

                    plugin.getUtils().sendMessage(sender, "messages.insights.reload");
                } else {
                    plugin.getUtils().sendMessage(sender, "messages.no_permission");
                }
                return true;
            } else if (args[0].equalsIgnoreCase("hooks")) {
                if (sender.hasPermission("insights.hooks")) {
                    List<String> plugins = new ArrayList<>();
                    plugin.getHookManager().getHooks().forEach(hook -> plugins.add(hook.getPlugin().getName()));

                    if (plugin.hasPlaceholderAPIHook()) {
                        plugins.add("PlaceHolderAPI");
                    }
                    if (plugin.getWorldGuardUtils() != null) {
                        plugins.add("WorldGuard");
                    }

                    if (plugins.size() > 0) {
                        plugin.getUtils().sendMessage(sender, "messages.insights.hooks.header");
                        plugins.forEach(pl -> plugin.getUtils().sendMessage(sender, "messages.insights.hooks.format", "%plugin%", pl));
                        plugin.getUtils().sendMessage(sender, "messages.insights.hooks.footer");
                    } else {
                        plugin.getUtils().sendMessage(sender, "messages.insights.hooks.none");
                    }
                } else {
                    plugin.getUtils().sendMessage(sender, "messages.no_permission");
                }
                return true;
            }
        }

        plugin.getUtils().sendMessage(sender, "messages.insights.help");
        return true;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>(Collections.singletonList("help"));
            if (sender.hasPermission("insights.reload")) {
                list.add("reload");
            }
            if (sender.hasPermission("insights.hooks")) {
                list.add("hooks");
            }
            return StringUtil.copyPartialMatches(args[0], list, new ArrayList<>());
        }
        return Collections.emptyList();
    }
}
