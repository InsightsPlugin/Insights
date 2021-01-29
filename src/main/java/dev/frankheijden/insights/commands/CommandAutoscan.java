package dev.frankheijden.insights.commands;

import dev.frankheijden.insights.Insights;
import dev.frankheijden.insights.utils.MessageUtils;
import dev.frankheijden.insights.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

public class CommandAutoscan implements CommandExecutor, TabExecutor {

    private static final Insights plugin = Insights.getInstance();

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
                            MessageUtils.sendMessage(sender, "messages.no_permission");
                            return true;
                        }
                        joiner.add(str);
                    }

                    plugin.getSqLite().setAutoScan(player.getUniqueId(), 0, joiner.toString());
                    MessageUtils.sendMessage(sender, "messages.autoscan.enabled");
                    return true;
                } else if (args.length == 2 && args[0].equalsIgnoreCase("limit")) {
                    if (!sender.hasPermission("insights.autoscan.limit")) {
                        MessageUtils.sendMessage(sender, "messages.no_permission");
                        return true;
                    }

                    plugin.getSqLite().setAutoScan(player.getUniqueId(), 1, args[1]);
                    MessageUtils.sendMessage(sender, "messages.autoscan.enabled");
                    return true;
                }
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("disable")) {
                    if (plugin.getSqLite().getAutoscan(player) != null) {
                        plugin.getSqLite().disableAutoScan(player.getUniqueId());
                        MessageUtils.sendMessage(sender, "messages.autoscan.disabled");
                    } else {
                        MessageUtils.sendMessage(sender, "messages.autoscan.not_enabled");
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
                return StringUtil.copyPartialMatches(args[args.length-1], Utils.SCANNABLE_MATERIALS, new ArrayList<>());
            } else if (args.length >= 2 && args[0].equalsIgnoreCase("entries") && args[args.length-1].length() > 0) {
                return StringUtil.copyPartialMatches(args[args.length-1], Utils.SCANNABLE_MATERIALS, new ArrayList<>());
            }
        }
        return Collections.emptyList();
    }
}
