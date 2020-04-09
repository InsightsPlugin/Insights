package net.frankheijden.insights.commands;

import net.frankheijden.insights.Insights;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;

public class CommandAutoscan implements CommandExecutor, TabExecutor {
    private Insights plugin;

    public CommandAutoscan(Insights plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length >= 2) {
                if (args[0].equalsIgnoreCase("entries")) {
                    StringJoiner joiner = new StringJoiner(",");
                    for (int i = 1; i < args.length; i++) {
                        String str = args[i];
                        if (!sender.hasPermission("insights.autoscan." + str)) {
                            plugin.getUtils().sendMessage(sender, "messages.no_permission");
                            return true;
                        }
                        joiner.add(str);
                    }

                    plugin.getSqLite().setAutoScan(player.getUniqueId(), 0, joiner.toString());
                    plugin.getUtils().sendMessage(sender, "messages.autoscan.enabled");
                    return true;
                } else if (args.length == 2 && args[0].equalsIgnoreCase("limit")) {
                    if (!sender.hasPermission("insights.autoscan.limit")) {
                        plugin.getUtils().sendMessage(sender, "messages.no_permission");
                        return true;
                    }

                    plugin.getSqLite().setAutoScan(player.getUniqueId(), 1, args[1]);
                    plugin.getUtils().sendMessage(sender, "messages.autoscan.enabled");
                    return true;
                }
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("disable")) {
                    if (plugin.getSqLite().getAutoscan(player) != null) {
                        plugin.getSqLite().disableAutoScan(player.getUniqueId());
                        plugin.getUtils().sendMessage(sender, "messages.autoscan.disabled");
                    } else {
                        plugin.getUtils().sendMessage(sender, "messages.autoscan.not_enabled");
                    }
                    return true;
                }
            }
        } else {
            sender.sendMessage("This command cannot be executed from console!");
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("insights.autoscan.tab")) {
            if (args.length == 1) {
                List<String> list = Arrays.asList("disable", "entries", "limit");
                return StringUtil.copyPartialMatches(args[0], list, new ArrayList<>());
            } else if (args.length == 2 && args[0].equalsIgnoreCase("limit") && args[1].length() > 0) {
                return StringUtil.copyPartialMatches(args[args.length-1], plugin.getUtils().getScannableMaterials(), new ArrayList<>());
            } else if (args.length >= 2 && args[0].equalsIgnoreCase("entries") && args[args.length-1].length() > 0) {
                return StringUtil.copyPartialMatches(args[args.length-1], plugin.getUtils().getScannableMaterials(), new ArrayList<>());
            }
        }
        return Collections.emptyList();
    }
}
