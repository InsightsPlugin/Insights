package net.frankheijden.insights.commands;

import net.frankheijden.insights.Insights;
import net.frankheijden.insights.api.builders.ScanTaskBuilder;
import net.frankheijden.insights.api.enums.ScanType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
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

public class CommandScanworld implements CommandExecutor, TabExecutor {
    private Insights plugin;

    public CommandScanworld(Insights plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        boolean tilePerm = sender.hasPermission("insights.scanworld.tile");
        boolean entityPerm = sender.hasPermission("insights.scanworld.entity");

        if (tilePerm || entityPerm) {
            if (args.length == 1) {
                if (tilePerm && entityPerm) {
                    World world = Bukkit.getWorld(args[0]);
                    if (world != null) {
                        if (sender instanceof Player) {
                            Player player = (Player) sender;
                            if (plugin.getPlayerScanTasks().containsKey(player.getUniqueId())) {
                                plugin.getUtils().sendMessage(sender, "messages.already_scanning");
                                return true;
                            }
                        }

                        long startTime = System.currentTimeMillis();

                        ScanTaskBuilder builder = new ScanTaskBuilder(plugin, ScanType.BOTH, world, plugin.getUtils().getChunkLocations(world.getLoadedChunks()))
                                .setCommandSenderAndPath(sender, "messages.scanworld");
                        builder.build().start(startTime);
                    } else {
                        plugin.getUtils().sendMessage(sender, "messages.scanworld.invalid_world");
                    }
                } else {
                    plugin.getUtils().sendMessage(sender, "messages.no_permission");
                }
            } else if (args.length == 2) {
                World world = Bukkit.getWorld(args[0]);
                if (args[1].equalsIgnoreCase("tile")) {
                    if (tilePerm) {
                        if (world != null) {
                            if (sender instanceof Player) {
                                Player player = (Player) sender;
                                if (plugin.getPlayerScanTasks().containsKey(player.getUniqueId())) {
                                    plugin.getUtils().sendMessage(sender, "messages.already_scanning");
                                    return true;
                                }
                            }

                            long startTime = System.currentTimeMillis();

                            ScanTaskBuilder builder = new ScanTaskBuilder(plugin, ScanType.TILE, world, plugin.getUtils().getChunkLocations(world.getLoadedChunks()))
                                    .setCommandSenderAndPath(sender, "messages.scanworld");
                            builder.build().start(startTime);
                        } else {
                            plugin.getUtils().sendMessage(sender, "messages.scanworld.invalid_world");
                        }
                    } else {
                        plugin.getUtils().sendMessage(sender, "messages.no_permission");
                    }
                } else if (args[1].equalsIgnoreCase("entity")) {
                    if (entityPerm) {
                        if (world != null) {
                            if (sender instanceof Player) {
                                Player player = (Player) sender;
                                if (plugin.getPlayerScanTasks().containsKey(player.getUniqueId())) {
                                    plugin.getUtils().sendMessage(sender, "messages.already_scanning");
                                    return true;
                                }
                            }

                            long startTime = System.currentTimeMillis();

                            ScanTaskBuilder builder = new ScanTaskBuilder(plugin, ScanType.ENTITY, world, plugin.getUtils().getChunkLocations(world.getLoadedChunks()))
                                    .setCommandSenderAndPath(sender, "messages.scanworld");
                            builder.build().start(startTime);
                        } else {
                            plugin.getUtils().sendMessage(sender, "messages.scanworld.invalid_world");
                        }
                    } else {
                        plugin.getUtils().sendMessage(sender, "messages.no_permission");
                    }
                } else if (args[1].equalsIgnoreCase("all")) {
                    if (sender.hasPermission("insights.scanradius.all")) {
                        if (world != null) {
                            if (sender instanceof Player) {
                                Player player = (Player) sender;
                                if (plugin.getPlayerScanTasks().containsKey(player.getUniqueId())) {
                                    plugin.getUtils().sendMessage(sender, "messages.already_scanning");
                                    return true;
                                }
                            }

                            long startTime = System.currentTimeMillis();

                            ScanTaskBuilder builder = new ScanTaskBuilder(plugin, ScanType.ALL, world, plugin.getUtils().getChunkLocations(world.getLoadedChunks()))
                                    .setCommandSenderAndPath(sender, "messages.scanworld");
                            builder.build().start(startTime);
                        } else {
                            plugin.getUtils().sendMessage(sender, "messages.scanworld.invalid_world");
                        }
                    } else {
                        plugin.getUtils().sendMessage(sender, "messages.no_permission");
                    }
                    return true;
                } else {
                    return false;
                }
            } else if (args.length > 2) {
                World world = Bukkit.getWorld(args[0]);
                if (args[1].equalsIgnoreCase("custom")) {
                    long startTime = System.currentTimeMillis();

                    ArrayList<Material> materials = new ArrayList<>();
                    ArrayList<EntityType> entityTypes = new ArrayList<>();
                    boolean isAll = false;
                    for (int i = 2; i < args.length; i++) {
                        Material material = Material.getMaterial(args[i]);
                        EntityType entityType = plugin.getUtils().getEntityType(args[i]);
                        if (material != null) {
                            if (sender.hasPermission("insights.scanworld.custom." + material.name())) {
                                materials.add(material);
                            } else {
                                plugin.getUtils().sendMessage(sender, "messages.no_permission");
                                return true;
                            }
                        } else if (entityType != null) {
                            if (sender.hasPermission("insights.scanworld.custom." + entityType.name())) {
                                entityTypes.add(entityType);
                            } else {
                                plugin.getUtils().sendMessage(sender, "messages.no_permission");
                                return true;
                            }
                        } else if (args[i].equalsIgnoreCase("ALL")) {
                            if (sender.hasPermission("insights.scan.custom.all")) {
                                isAll = true;
                            } else {
                                plugin.getUtils().sendMessage(sender, "messages.no_permission");
                                return true;
                            }
                        } else {
                            plugin.getUtils().sendMessage(sender, "messages.scanworld.custom.invalid_argument", "%argument%", args[i]);
                            return true;
                        }
                    }

                    if (materials.isEmpty() && entityTypes.isEmpty() && !isAll) return true;

                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        if (plugin.getPlayerScanTasks().containsKey(player.getUniqueId())) {
                            plugin.getUtils().sendMessage(sender, "messages.already_scanning");
                            return true;
                        }
                    }

                    if (world != null) {
                        ScanTaskBuilder builder = new ScanTaskBuilder(plugin, ScanType.CUSTOM, world, plugin.getUtils().getChunkLocations(world.getLoadedChunks()))
                                .setCommandSenderAndPath(sender, "messages.scanworld")
                                .setMaterials(materials)
                                .setEntityTypes(entityTypes);
                        builder.build().start(startTime);
                    } else {
                        plugin.getUtils().sendMessage(sender, "messages.scanworld.invalid_world");
                    }
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            plugin.getUtils().sendMessage(sender, "messages.no_permission");
        }
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
                List<String> list = Arrays.asList("all", "custom", "entity", "tile");
                return StringUtil.copyPartialMatches(args[1], list, new ArrayList<>());
            } else if (args.length > 2 && args[1].equalsIgnoreCase("custom") && args[args.length-1].length() > 0) {
                return StringUtil.copyPartialMatches(args[args.length-1], plugin.getUtils().getScannableMaterials(), new ArrayList<>());
            }
        }
        return Collections.emptyList();
    }
}
