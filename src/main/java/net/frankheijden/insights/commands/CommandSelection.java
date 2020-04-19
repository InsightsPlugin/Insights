package net.frankheijden.insights.commands;

import net.frankheijden.insights.builders.Scanner;
import net.frankheijden.insights.entities.*;
import net.frankheijden.insights.enums.ScanType;
import net.frankheijden.insights.managers.SelectionManager;
import net.frankheijden.insights.utils.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;

public class CommandSelection implements CommandExecutor, TabExecutor {

    private static final SelectionManager selectionManager = SelectionManager.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command cannot be executed from console!");
            return true;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        ScanOptions scanOptions = new ScanOptions();
        scanOptions.setWorld(player.getWorld());
        scanOptions.setUUIDAndPath(player.getUniqueId(), "messages.selection.scan");

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("create")) {
                if (player.hasPermission("insights.selection.create")) {
                    if (selectionManager.isSelecting(uuid)) {
                        MessageUtils.sendMessage(player, "messages.selection.already_selecting");
                    } else {
                        selectionManager.setSelecting(uuid);
                        MessageUtils.sendMessage(player, "messages.selection.create.info");
                    }
                } else {
                    MessageUtils.sendMessage(sender, "messages.no_permission");
                }
                return true;
            } else if (args[0].equalsIgnoreCase("stop")) {
                if (player.hasPermission("insights.selection.create")) {
                    if (!selectionManager.isSelecting(uuid)) {
                        MessageUtils.sendMessage(player, "messages.selection.not_selecting");
                    } else {
                        selectionManager.removeSelecting(uuid);
                        MessageUtils.sendMessage(player, "messages.selection.stop");
                    }
                } else {
                    MessageUtils.sendMessage(sender, "messages.no_permission");
                }
                return true;
            } else if (args[0].equalsIgnoreCase("scan")) {
                if (player.hasPermission("insights.selection.scan.all")) {
                    scanOptions.setScanType(ScanType.ALL);
                } else {
                    MessageUtils.sendMessage(sender, "messages.no_permission");
                    return true;
                }
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("scan")) {
                if (args[1].equalsIgnoreCase("tile")) {
                    if (player.hasPermission("insights.selection.scan.tile")) {
                        scanOptions.setScanType(ScanType.TILE);
                    } else {
                        MessageUtils.sendMessage(sender, "messages.no_permission");
                        return true;
                    }
                } else if (args[1].equalsIgnoreCase("entity")) {
                    if (player.hasPermission("insights.selection.scan.entity")) {
                        scanOptions.setScanType(ScanType.ENTITY);
                    } else {
                        MessageUtils.sendMessage(sender, "messages.no_permission");
                        return true;
                    }
                }
            }
        } else if (args[0].equalsIgnoreCase("scan") && args[1].equalsIgnoreCase("custom")) {
            scanOptions.setScanType(ScanType.CUSTOM);
            ArrayList<String> strings = new ArrayList<>();
            for (int i = 2; i < args.length; i++) {
                String str = args[i];

                if (sender.hasPermission("insights.selection.scan.custom." + str)) {
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

        Selection selection = selectionManager.getSelection(uuid);
        if (!selection.isValid()) {
            MessageUtils.sendMessage(player, "messages.selection.invalid_selection");
            return true;
        }

        List<PartialChunk> partials = ChunkUtils.getPartialChunks(selection.getPos1(), selection.getPos2());
        scanOptions.setPartialChunks(partials);
        Scanner.create(scanOptions).scan();
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("insights.selection.tab")) {
            if (args.length == 1) {
                List<String> list = Arrays.asList("create", "scan", "stop");
                return StringUtil.copyPartialMatches(args[0], list, new ArrayList<>());
            } else if (args.length == 2 && args[0].equalsIgnoreCase("scan")) {
                List<String> list = Arrays.asList("custom", "entity", "tile");
                return StringUtil.copyPartialMatches(args[1], list, new ArrayList<>());
            } else if (args.length > 2 && args[1].equalsIgnoreCase("custom") && args[args.length-1].length() > 0) {
                return StringUtil.copyPartialMatches(args[args.length-1], Utils.SCANNABLE_MATERIALS, new ArrayList<>());
            }
        }
        return Collections.emptyList();
    }
}
