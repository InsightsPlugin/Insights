package net.frankheijden.insights.commands;

import net.frankheijden.insights.Insights;
import net.frankheijden.insights.builders.Scanner;
import net.frankheijden.insights.entities.ScanOptions;
import net.frankheijden.insights.enums.ScanType;
import net.frankheijden.insights.utils.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;

public class CommandScanradius implements CommandExecutor, TabExecutor {

    private static final Insights plugin = Insights.getInstance();
    private static final int MAX_SCAN_RADIUS = 25;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        boolean tilePerm = sender.hasPermission("insights.scanradius.tile");
        boolean entityPerm = sender.hasPermission("insights.scanradius.entity");

        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (plugin.isPlayerScanning(player, true)) return true;

            ScanOptions scanOptions = new ScanOptions();
            scanOptions.setWorld(player.getWorld());
            scanOptions.setUUIDAndPath(player.getUniqueId(), "messages.scanradius");

            Integer radius = parseValidRadius(sender, args, 0);
            if (radius == null) return true;

            scanOptions.setChunkLocations(new LinkedList<>(ChunkUtils
                    .getChunkLocations(player.getLocation().getChunk(), radius)));

            if (args.length == 1) {
                if (player.hasPermission("insights.scanradius.all")) {
                    scanOptions.setScanType(ScanType.ALL);
                } else {
                    MessageUtils.sendMessage(player, "messages.no_permission");
                    return true;
                }
            } else if (args.length == 2) {
                if (args[1].equalsIgnoreCase("entity")) {
                    if (entityPerm) {
                        scanOptions.setScanType(ScanType.ENTITY);
                    } else {
                        MessageUtils.sendMessage(sender, "messages.no_permission");
                        return true;
                    }
                } else if (args[1].equalsIgnoreCase("tile")) {
                    if (tilePerm) {
                        scanOptions.setScanType(ScanType.TILE);
                    } else {
                        MessageUtils.sendMessage(sender, "messages.no_permission");
                        return true;
                    }
                }
            } else if (args.length > 2 && args[1].equalsIgnoreCase("custom")) {
                scanOptions.setScanType(ScanType.CUSTOM);
                ArrayList<String> strings = new ArrayList<>();
                for (int i = 2; i < args.length; i++) {
                    String str = args[i];

                    if (sender.hasPermission("insights.scanradius.custom." + str)) {
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

    private Integer parseValidRadius(CommandSender sender, String[] args, int pos) {
        if (args.length < pos || !args[pos].matches("-?(0|[1-9]\\d*)")) {
            MessageUtils.sendMessage(sender, "messages.scanradius.invalid_number");
        } else {
            int radius = Integer.parseInt(args[pos]);
            if (radius > MAX_SCAN_RADIUS) {
                MessageUtils.sendMessage(sender, "messages.scanradius.radius_too_large");
            } else if (radius < 1) {
                MessageUtils.sendMessage(sender, "messages.scanradius.radius_too_small");
            } else {
                return radius;
            }
        }
        return null;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("insights.scanradius.tab")) {
            if (args.length == 1) {
                return StringUtil.copyPartialMatches(args[0], Collections.singletonList(String.valueOf(plugin.getConfiguration().GENERAL_SCANRADIUS_DEFAULT)), new ArrayList<>());
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
