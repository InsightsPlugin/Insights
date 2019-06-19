package net.frankheijden.insights.commands;

import net.frankheijden.insights.Insights;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.EntityType;
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
            if (args.length == 2) {
                if (args[0].equalsIgnoreCase("enable")) {
                    Material material = Material.getMaterial(args[1]);
                    EntityType entityType = plugin.getUtils().getEntityType(args[1]);
                    if (material != null) {
                        if (sender.hasPermission("insights.autoscan." + material.name())) {
                            plugin.getSqLite().setAutoScan(player.getUniqueId(), material.name());
                            plugin.getUtils().sendMessage(sender, "messages.autoscan.enabled_material", "%material%", plugin.getUtils().capitalizeName(material.name()));
                        } else {
                            plugin.getUtils().sendMessage(sender, "messages.no_permission");
                        }
                    } else if (entityType != null) {
                        if (sender.hasPermission("insights.autoscan." + entityType.name())) {
                            plugin.getSqLite().setAutoScan(player.getUniqueId(), entityType.name());
                            plugin.getUtils().sendMessage(sender, "messages.autoscan.enabled_entity", "%entity%", plugin.getUtils().capitalizeName(entityType.name()));
                        } else {
                            plugin.getUtils().sendMessage(sender, "messages.no_permission");
                        }
                    } else {
                        plugin.getUtils().sendMessage(sender, "messages.autoscan.invalid_argument", "%argument%", args[1]);
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("disable")) {
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
                List<String> list = Arrays.asList("disable", "enable");
                return StringUtil.copyPartialMatches(args[0], list, new ArrayList<>());
            } else if (args.length == 2 && args[0].equalsIgnoreCase("enable") && args[1].length() > 0) {
                TreeSet<String> list = new TreeSet<>();
                for (Material material : Material.values()) {
                    list.add(material.name());
                }
                for (EntityType entityType : EntityType.values()) {
                    list.add(entityType.name());
                }
                return StringUtil.copyPartialMatches(args[args.length-1], list, new ArrayList<>());
            }
        }
        return Collections.emptyList();
    }
}
