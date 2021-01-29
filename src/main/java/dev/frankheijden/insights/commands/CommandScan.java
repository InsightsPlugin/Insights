package dev.frankheijden.insights.commands;

import dev.frankheijden.insights.entities.PartialChunk;
import dev.frankheijden.insights.entities.ScanOptions;
import dev.frankheijden.insights.managers.ScanManager;
import dev.frankheijden.insights.builders.Scanner;
import dev.frankheijden.insights.enums.ScanType;
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

public class CommandScan implements CommandExecutor, TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (ScanManager.getInstance().isScanning(player)) return true;

            ScanOptions scanOptions = new ScanOptions();
            scanOptions.setWorld(player.getWorld());
            scanOptions.addPartialChunk(PartialChunk.from(player.getLocation().getChunk()));
            scanOptions.setUUIDAndPath(player.getUniqueId(), "messages.scan");

            if (args.length == 0) {
                if (player.hasPermission("insights.scan.all")) {
                    scanOptions.setScanType(ScanType.ALL);
                } else {
                    MessageUtils.sendMessage(player, "messages.no_permission");
                    return true;
                }
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("entity")) {
                    if (player.hasPermission("insights.scan.entity")) {
                        scanOptions.setScanType(ScanType.ENTITY);
                    } else {
                        MessageUtils.sendMessage(player, "messages.no_permission");
                        return true;
                    }
                } else if (args[0].equalsIgnoreCase("tile")) {
                    if (player.hasPermission("insights.scan.tile")) {
                        scanOptions.setScanType(ScanType.TILE);
                    } else {
                        MessageUtils.sendMessage(player, "messages.no_permission");
                        return true;
                    }
                }
            } else if (args[0].equalsIgnoreCase("custom")) {
                scanOptions.setScanType(ScanType.CUSTOM);
                ArrayList<String> strings = new ArrayList<>();
                for (int i = 1; i < args.length; i++) {
                    String str = args[i];

                    if (sender.hasPermission("insights.scan.custom." + str)) {
                        strings.add(str);
                    } else {
                        MessageUtils.sendMessage(sender, "messages.no_permission");
                        return true;
                    }
                }

                scanOptions.setMaterials(strings);
                scanOptions.setEntityTypes(strings);
            }

            if (scanOptions.getScanType() == null) return false;
            Scanner.create(scanOptions).scan();
        } else {
            sender.sendMessage("This command cannot be executed from console!");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("insights.scan.tab")) {
            if (args.length == 1) {
                List<String> list = Arrays.asList("custom", "entity", "tile");
                return StringUtil.copyPartialMatches(args[0], list, new ArrayList<>());
            } else if (args.length > 1 && args[0].equalsIgnoreCase("custom") && args[args.length-1].length() > 0) {
                return StringUtil.copyPartialMatches(args[args.length-1], Utils.SCANNABLE_MATERIALS, new ArrayList<>());
            }
        }
        return Collections.emptyList();
    }
}
