package net.frankheijden.insights.commands;

import net.frankheijden.insights.Insights;
import net.frankheijden.insights.api.builders.ScanTaskBuilder;
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

public class CommandScanradius implements CommandExecutor, TabExecutor {
    private Insights plugin;

    public CommandScanradius(Insights plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        boolean tilePerm = sender.hasPermission("insights.scanradius.tile");
        boolean entityPerm = sender.hasPermission("insights.scanradius.entity");

        if (tilePerm || entityPerm) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                if (args.length == 1) {
                    if (entityPerm && tilePerm) {
                        if (args[0].matches("-?(0|[1-9]\\d*)")) {
                            int radius = Integer.parseInt(args[0]);
                            if (radius >= 1 && radius <= 25) {
                                if (plugin.getPlayerScanTasks().containsKey(player.getUniqueId())) {
                                    plugin.getUtils().sendMessage(sender, "messages.already_scanning");
                                    return true;
                                }

                                long startTime = System.currentTimeMillis();

                                ScanTaskBuilder builder = new ScanTaskBuilder(plugin, ScanType.BOTH, player.getWorld(), plugin.getUtils().getChunkLocations(player.getLocation().getChunk(), radius))
                                        .setUUIDAndPath(player.getUniqueId(), "messages.scanradius")
                                        .setSaveWorld(true);
                                builder.build().start(startTime);
                                return true;
                            } else {
                                plugin.getUtils().sendMessage(sender, "messages.scanradius.invalid_radius");
                            }
                        } else {
                            plugin.getUtils().sendMessage(sender, "messages.scanradius.invalid_number");
                        }
                    } else {
                        plugin.getUtils().sendMessage(sender, "messages.no_permission");
                    }
                    return true;
                } else if (args.length == 2) {
                    if (args[0].matches("-?(0|[1-9]\\d*)")) {
                        int radius = Integer.parseInt(args[0]);
                        if (radius >= 1 && radius <= 25) {
                            if (args[1].equalsIgnoreCase("entity")) {
                                if (entityPerm) {
                                    if (plugin.getPlayerScanTasks().containsKey(player.getUniqueId())) {
                                        plugin.getUtils().sendMessage(sender, "messages.already_scanning");
                                        return true;
                                    }

                                    long startTime = System.currentTimeMillis();

                                    ScanTaskBuilder builder = new ScanTaskBuilder(plugin, ScanType.ENTITY, player.getWorld(), plugin.getUtils().getChunkLocations(player.getLocation().getChunk(), radius))
                                            .setUUIDAndPath(player.getUniqueId(), "messages.scanradius")
                                            .setSaveWorld(true);
                                    builder.build().start(startTime);
                                    return true;
                                } else {
                                    plugin.getUtils().sendMessage(sender, "messages.no_permission");
                                }
                            } else if (args[1].equalsIgnoreCase("tile")) {
                                if (tilePerm) {
                                    if (plugin.getPlayerScanTasks().containsKey(player.getUniqueId())) {
                                        plugin.getUtils().sendMessage(sender, "messages.already_scanning");
                                        return true;
                                    }

                                    long startTime = System.currentTimeMillis();

                                    ScanTaskBuilder builder = new ScanTaskBuilder(plugin, ScanType.TILE, player.getWorld(), plugin.getUtils().getChunkLocations(player.getLocation().getChunk(), radius))
                                            .setUUIDAndPath(player.getUniqueId(), "messages.scanradius")
                                            .setSaveWorld(true);
                                    builder.build().start(startTime);
                                    return true;
                                } else {
                                    plugin.getUtils().sendMessage(sender, "messages.no_permission");
                                }
                            } else if (args[1].equalsIgnoreCase("all")) {
                                if (player.hasPermission("insights.scanradius.all")) {
                                    if (plugin.getPlayerScanTasks().containsKey(player.getUniqueId())) {
                                        plugin.getUtils().sendMessage(sender, "messages.already_scanning");
                                        return true;
                                    }

                                    long startTime = System.currentTimeMillis();

                                    ScanTaskBuilder builder = new ScanTaskBuilder(plugin, ScanType.ALL, player.getWorld(), plugin.getUtils().getChunkLocations(player.getLocation().getChunk(), radius))
                                            .setUUIDAndPath(player.getUniqueId(), "messages.scan")
                                            .setSaveWorld(true);
                                    builder.build().start(startTime);
                                } else {
                                    plugin.getUtils().sendMessage(player, "messages.no_permission");
                                }
                                return true;
                            }
                        } else {
                            plugin.getUtils().sendMessage(sender, "messages.scanradius.invalid_radius");
                        }
                    } else {
                        plugin.getUtils().sendMessage(sender, "messages.scanradius.invalid_number");
                    }
                } else if (args.length > 2) {
                    if (args[0].matches("-?(0|[1-9]\\d*)")) {
                        int radius = Integer.parseInt(args[0]);
                        if (radius <= 25) {
                            if (radius >= 1) {
                                if (args[1].equalsIgnoreCase("custom")) {
                                    long startTime = System.currentTimeMillis();

                                    ArrayList<Material> materials = new ArrayList<>();
                                    ArrayList<EntityType> entityTypes = new ArrayList<>();
                                    for (int i = 2; i < args.length; i++) {
                                        Material material = Material.getMaterial(args[i]);
                                        EntityType entityType = plugin.getUtils().getEntityType(args[i]);
                                        if (material != null) {
                                            if (sender.hasPermission("insights.scanradius.custom." + material.name())) {
                                                materials.add(material);
                                            } else {
                                                plugin.getUtils().sendMessage(sender, "messages.no_permission");
                                                return true;
                                            }
                                        } else if (entityType != null) {
                                            if (sender.hasPermission("insights.scanradius.custom." + entityType.name())) {
                                                entityTypes.add(entityType);
                                            } else {
                                                plugin.getUtils().sendMessage(sender, "messages.no_permission");
                                                return true;
                                            }
                                        } else {
                                            plugin.getUtils().sendMessage(sender, "messages.scanradius.custom.invalid_argument", "%argument%", args[i]);
                                            return true;
                                        }
                                    }

                                    if (plugin.getPlayerScanTasks().containsKey(player.getUniqueId())) {
                                        plugin.getUtils().sendMessage(sender, "messages.already_scanning");
                                        return true;
                                    }

                                    ScanTaskBuilder builder = new ScanTaskBuilder(plugin, ScanType.CUSTOM, player.getWorld(), plugin.getUtils().getChunkLocations(player.getLocation().getChunk(), radius))
                                            .setUUIDAndPath(player.getUniqueId(), "messages.scanradius")
                                            .setMaterials(materials)
                                            .setEntityTypes(entityTypes)
                                            .setSaveWorld(true);
                                    builder.build().start(startTime);
                                    return true;
                                } else {
                                    return false;
                                }
                            } else {
                                plugin.getUtils().sendMessage(sender, "messages.scanradius.radius_too_small");
                            }
                        } else {
                            plugin.getUtils().sendMessage(sender, "messages.scanradius.radius_too_large");
                        }
                    } else {
                        plugin.getUtils().sendMessage(sender, "messages.scanradius.invalid_number");
                    }
                } else {
                    return false;
                }
            } else {
                sender.sendMessage("This command cannot be executed from console!");
            }
        } else {
            plugin.getUtils().sendMessage(sender, "messages.no_permission");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("insights.scanradius.tab")) {
            if (args.length == 1) {
                return StringUtil.copyPartialMatches(args[0], Collections.singletonList(String.valueOf(plugin.getConfiguration().GENERAL_SCANRADIUS_DEFAULT)), new ArrayList<>());
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
