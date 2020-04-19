package net.frankheijden.insights.commands;

import net.frankheijden.insights.builders.Scanner;
import net.frankheijden.insights.entities.*;
import net.frankheijden.insights.enums.ScanType;
import net.frankheijden.insights.managers.ScanManager;
import net.frankheijden.insights.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;

public class CommandScanworld implements CommandExecutor, TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        boolean tilePerm = sender.hasPermission("insights.scanworld.tile");
        boolean entityPerm = sender.hasPermission("insights.scanworld.entity");

        if (args.length < 1) return false;
        World world = Bukkit.getWorld(args[0]);
        if (world == null) {
            MessageUtils.sendMessage(sender, "messages.scanworld.invalid_world");
            return true;
        }

        if (sender instanceof Player) {
            if (ScanManager.getInstance().isScanning((Player) sender)) return true;
        }

        ScanOptions scanOptions = new ScanOptions();
        scanOptions.setWorld(world);
        List<ChunkLocation> chunkLocations = ChunkLocation.from(world.getLoadedChunks());
        scanOptions.setPartialChunks(PartialChunk.from(world, chunkLocations));
        scanOptions.setCommandSenderAndPath(sender, "messages.scanworld");

        if (args.length == 1) {
            if (sender.hasPermission("insights.scanradius.all")) {
                scanOptions.setScanType(ScanType.ALL);
            } else {
                MessageUtils.sendMessage(sender, "messages.no_permission");
                return true;
            }
        } else if (args.length == 2) {
            if (args[1].equalsIgnoreCase("tile")) {
                if (tilePerm) {
                    scanOptions.setScanType(ScanType.TILE);
                } else {
                    MessageUtils.sendMessage(sender, "messages.no_permission");
                    return true;
                }
            } else if (args[1].equalsIgnoreCase("entity")) {
                if (entityPerm) {
                    scanOptions.setScanType(ScanType.ENTITY);
                } else {
                    MessageUtils.sendMessage(sender, "messages.no_permission");
                    return true;
                }
            }
        } else if (args[1].equalsIgnoreCase("custom")) {
            scanOptions.setScanType(ScanType.CUSTOM);
            ArrayList<String> strings = new ArrayList<>();
            for (int i = 2; i < args.length; i++) {
                String str = args[i];

                if (sender.hasPermission("insights.scanworld.custom." + str)) {
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
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("insights.scanworld.tab")) {
            if (args.length == 1) {
                List<String> list = new ArrayList<>();
                for (World world : Bukkit.getWorlds()) {
                    list.add(world.getName());
                }
                return StringUtil.copyPartialMatches(args[0], list, new ArrayList<>());
            } else if (args.length == 2) {
                List<String> list = Arrays.asList("custom", "entity", "tile");
                return StringUtil.copyPartialMatches(args[1], list, new ArrayList<>());
            } else if (args.length > 2 && args[1].equalsIgnoreCase("custom") && args[args.length-1].length() > 0) {
                return StringUtil.copyPartialMatches(args[args.length-1], Utils.SCANNABLE_MATERIALS, new ArrayList<>());
            }
        }
        return Collections.emptyList();
    }
}
