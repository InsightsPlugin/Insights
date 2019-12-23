package net.frankheijden.insights.commands;

import net.frankheijden.insights.Insights;
import net.frankheijden.insights.api.builders.ScanTaskBuilder;
import net.frankheijden.insights.api.entities.ChunkLocation;
import net.frankheijden.insights.api.enums.ScanType;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandScan implements CommandExecutor, TabExecutor {
    private Insights plugin;

    public CommandScan(Insights plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (args.length == 0) {
                if (player.hasPermission("insights.scan.entity") && player.hasPermission("insights.scan.tile")) {
                    if (plugin.getPlayerScanTasks().containsKey(player.getUniqueId())) {
                        plugin.getUtils().sendMessage(sender, "messages.already_scanning");
                        return true;
                    }

                    long startTime = System.currentTimeMillis();

                    ScanTaskBuilder builder = new ScanTaskBuilder(plugin, ScanType.BOTH, player.getWorld(), Collections.singletonList(new ChunkLocation(player.getLocation().getChunk())))
                            .setUUIDAndPath(player.getUniqueId(), "messages.scan");
                    builder.build().start(startTime);
                } else {
                    plugin.getUtils().sendMessage(player, "messages.no_permission");
                }
                return true;
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("entity")) {
                    if (player.hasPermission("insights.scan.entity")) {
                        if (plugin.getPlayerScanTasks().containsKey(player.getUniqueId())) {
                            plugin.getUtils().sendMessage(sender, "messages.already_scanning");
                            return true;
                        }

                        long startTime = System.currentTimeMillis();

                        ScanTaskBuilder builder = new ScanTaskBuilder(plugin, ScanType.ENTITY, player.getWorld(), Collections.singletonList(new ChunkLocation(player.getLocation().getChunk())))
                                .setUUIDAndPath(player.getUniqueId(), "messages.scan");
                        builder.build().start(startTime);
                    } else {
                        plugin.getUtils().sendMessage(player, "messages.no_permission");
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("tile")) {
                    if (player.hasPermission("insights.scan.tile")) {
                        if (plugin.getPlayerScanTasks().containsKey(player.getUniqueId())) {
                            plugin.getUtils().sendMessage(sender, "messages.already_scanning");
                            return true;
                        }

                        long startTime = System.currentTimeMillis();

                        ScanTaskBuilder builder = new ScanTaskBuilder(plugin, ScanType.TILE, player.getWorld(), Collections.singletonList(new ChunkLocation(player.getLocation().getChunk())))
                                .setUUIDAndPath(player.getUniqueId(), "messages.scan");
                        builder.build().start(startTime);
                    } else {
                        plugin.getUtils().sendMessage(player, "messages.no_permission");
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("all")) {
                    if (player.hasPermission("insights.scan.all")) {
                        if (plugin.getPlayerScanTasks().containsKey(player.getUniqueId())) {
                            plugin.getUtils().sendMessage(sender, "messages.already_scanning");
                            return true;
                        }

                        long startTime = System.currentTimeMillis();

                        ScanTaskBuilder builder = new ScanTaskBuilder(plugin, ScanType.ALL, player.getWorld(), Collections.singletonList(new ChunkLocation(player.getLocation().getChunk())))
                                .setUUIDAndPath(player.getUniqueId(), "messages.scan");
                        builder.build().start(startTime);
                    } else {
                        plugin.getUtils().sendMessage(player, "messages.no_permission");
                    }
                    return true;
                }
            } else {
                if (args[0].equalsIgnoreCase("custom")) {
                    long startTime = System.currentTimeMillis();

                    ArrayList<Material> materials = new ArrayList<>();
                    ArrayList<EntityType> entityTypes = new ArrayList<>();
                    for (int i = 1; i < args.length; i++) {
                        Material material = Material.getMaterial(args[i]);
                        EntityType entityType = plugin.getUtils().getEntityType(args[i]);
                        if (material != null) {
                            if (sender.hasPermission("insights.scan.custom." + material.name())) {
                                materials.add(material);
                            } else {
                                plugin.getUtils().sendMessage(sender, "messages.no_permission");
                                return true;
                            }
                        } else if (entityType != null) {
                            if (sender.hasPermission("insights.scan.custom." + entityType.name())) {
                                entityTypes.add(entityType);
                            } else {
                                plugin.getUtils().sendMessage(sender, "messages.no_permission");
                                return true;
                            }
                        } else {
                            plugin.getUtils().sendMessage(sender, "messages.scan.custom.invalid_argument", "%argument%", args[i]);
                            return true;
                        }
                    }

                    if (plugin.getPlayerScanTasks().containsKey(player.getUniqueId())) {
                        plugin.getUtils().sendMessage(sender, "messages.already_scanning");
                        return true;
                    }

                    ScanTaskBuilder builder = new ScanTaskBuilder(plugin, ScanType.CUSTOM, player.getWorld(), Collections.singletonList(new ChunkLocation(player.getLocation().getChunk())))
                            .setUUIDAndPath(player.getUniqueId(), "messages.scan")
                            .setMaterials(materials)
                            .setEntityTypes(entityTypes);
                    builder.build().start(startTime);
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
        if (sender.hasPermission("insights.scan.tab")) {
            if (args.length == 1) {
                List<String> list = Arrays.asList("all", "custom", "entity", "tile");
                return StringUtil.copyPartialMatches(args[0], list, new ArrayList<>());
            } else if (args.length > 1 && args[0].equalsIgnoreCase("custom") && args[args.length-1].length() > 0) {
                return StringUtil.copyPartialMatches(args[args.length-1], plugin.getUtils().getScannableMaterials(), new ArrayList<>());
            }
        }
        return Collections.emptyList();
    }
}
