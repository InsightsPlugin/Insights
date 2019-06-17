package net.frankheijden.insights.commands;

import net.frankheijden.insights.Insights;
import net.frankheijden.insights.api.entities.ChunkLocation;
import net.frankheijden.insights.tasks.ScanTask;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.StringUtil;

import java.text.NumberFormat;
import java.util.*;

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
                        int totalEntityCount = 0;
                        int totalTileCount = 0;
                        TreeMap<String, Integer> entryTreeMap = new TreeMap<>();
                        for (Chunk chunk : world.getLoadedChunks()) {
                            for (BlockState bs : chunk.getTileEntities()) {
                                entryTreeMap.merge(bs.getType().name(), 1, Integer::sum);
                                totalTileCount++;
                            }
                        }
                        for (Entity entity : world.getEntities()) {
                            entryTreeMap.merge(entity.getType().name(), 1, Integer::sum);
                            totalEntityCount++;
                        }

                        if (entryTreeMap.size() > 0) {
                            plugin.utils.sendMessage(sender, "messages.scanworld.both.header");

                            for (Map.Entry<String, Integer> entry : entryTreeMap.entrySet()) {
                                String name = plugin.utils.capitalizeName(entry.getKey().toLowerCase());
                                plugin.utils.sendMessage(sender, "messages.scanworld.both.format", "%entry%", name, "%count%", NumberFormat.getIntegerInstance().format(entry.getValue()));

                            }

                            plugin.utils.sendMessage(sender, "messages.scanworld.both.total", "%world%", world.getName(), "%entities%", NumberFormat.getIntegerInstance().format(totalEntityCount), "%tiles%", NumberFormat.getIntegerInstance().format(totalTileCount));
                            plugin.utils.sendMessage(sender, "messages.scanworld.both.footer");
                        } else {
                            plugin.utils.sendMessage(sender, "messages.scanworld.both.no_entries", "%world%", world.getName());
                        }
                    } else {
                        plugin.utils.sendMessage(sender, "messages.scanworld.invalid_world");
                    }
                } else {
                    plugin.utils.sendMessage(sender, "messages.no_permission");
                }
            } else if (args.length == 2) {
                World world = Bukkit.getWorld(args[0]);
                if (args[1].equalsIgnoreCase("tile")) {
                    if (tilePerm) {
                        if (world != null) {
                            TreeMap<String, Integer> tileTreeMap = new TreeMap<>();
                            for (Chunk chunk : world.getLoadedChunks()) {
                                for (BlockState bs : chunk.getTileEntities()) {
                                    tileTreeMap.merge(bs.getType().name(), 1, Integer::sum);
                                }
                            }

                            if (tileTreeMap.size() > 0) {
                                plugin.utils.sendMessage(sender, "messages.scanworld.tile.header");

                                int totalTileCount = 0;
                                for (Map.Entry<String, Integer> entry : tileTreeMap.entrySet()) {
                                    String name = plugin.utils.capitalizeName(entry.getKey().toLowerCase());
                                    plugin.utils.sendMessage(sender, "messages.scanworld.tile.format", "%tile%", name, "%count%", NumberFormat.getIntegerInstance().format(entry.getValue()));

                                    totalTileCount = totalTileCount + entry.getValue();
                                }

                                plugin.utils.sendMessage(sender, "messages.scanworld.tile.total", "%world%", world.getName(), "%total_count%", NumberFormat.getIntegerInstance().format(totalTileCount));
                                plugin.utils.sendMessage(sender, "messages.scanworld.tile.footer");
                            } else {
                                plugin.utils.sendMessage(sender, "messages.scanworld.tile.no_tiles", "%world%", world.getName());
                            }
                        } else {
                            plugin.utils.sendMessage(sender, "messages.scanworld.invalid_world");
                        }
                    } else {
                        plugin.utils.sendMessage(sender, "messages.no_permission");
                    }
                } else if (args[1].equalsIgnoreCase("entity")) {
                    if (entityPerm) {
                        if (world != null) {
                            TreeMap<String, Integer> entityTreeMap = new TreeMap<>();
                            for (Entity entity : world.getEntities()) {
                                entityTreeMap.merge(entity.getType().name(), 1, Integer::sum);
                            }

                            if (entityTreeMap.size() > 0) {
                                plugin.utils.sendMessage(sender, "messages.scanworld.entity.header");

                                int totalEntityCount = 0;
                                for (Map.Entry<String, Integer> entry : entityTreeMap.entrySet()) {
                                    String name = plugin.utils.capitalizeName(entry.getKey().toLowerCase());
                                    plugin.utils.sendMessage(sender, "messages.scanworld.entity.format", "%entity%", name, "%count%", NumberFormat.getIntegerInstance().format(entry.getValue()));

                                    totalEntityCount = totalEntityCount + entry.getValue();
                                }

                                plugin.utils.sendMessage(sender, "messages.scanworld.entity.total", "%world%", world.getName(), "%total_count%", NumberFormat.getIntegerInstance().format(totalEntityCount));
                                plugin.utils.sendMessage(sender, "messages.scanworld.entity.footer");
                            } else {
                                plugin.utils.sendMessage(sender, "messages.scanworld.entity.no_entities", "%world%", world.getName());
                            }
                        } else {
                            plugin.utils.sendMessage(sender, "messages.scanworld.invalid_world");
                        }
                    } else {
                        plugin.utils.sendMessage(sender, "messages.no_permission");
                    }
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
                        EntityType entityType = plugin.utils.getEntityType(args[i]);
                        if (material != null) {
                            if (sender.hasPermission("insights.scanworld.custom." + material.name())) {
                                materials.add(material);
                            } else {
                                plugin.utils.sendMessage(sender, "messages.no_permission");
                                return true;
                            }
                        } else if (entityType != null) {
                            if (sender.hasPermission("insights.scanworld.custom." + entityType.name())) {
                                entityTypes.add(entityType);
                            } else {
                                plugin.utils.sendMessage(sender, "messages.no_permission");
                                return true;
                            }
                        } else if (args[i].equalsIgnoreCase("ALL")) {
                            if (sender.hasPermission("insights.scan.custom.all")) {
                                isAll = true;
                            } else {
                                plugin.utils.sendMessage(sender, "messages.no_permission");
                                return true;
                            }
                        } else {
                            plugin.utils.sendMessage(sender, "messages.scanworld.custom.invalid_argument", "%argument%", args[i]);
                            return true;
                        }
                    }

                    if (materials.isEmpty() && entityTypes.isEmpty() && !isAll) return true;

                    if (world != null) {
                        List<ChunkLocation> chunkLocations = new ArrayList<>();
                        Chunk[] chunks = world.getLoadedChunks();

                        for (Chunk chunk : chunks) {
                            chunkLocations.add(new ChunkLocation(chunk));
                        }

                        ScanTask task = new ScanTask(plugin, world, "messages.scanworld.custom", chunkLocations, materials, entityTypes, null);
                        task.start(startTime);
                    } else {
                        plugin.utils.sendMessage(sender, "messages.scanworld.invalid_world");
                    }
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            plugin.utils.sendMessage(sender, "messages.no_permission");
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
                List<String> list = Arrays.asList("custom", "entity", "tile");
                return StringUtil.copyPartialMatches(args[1], list, new ArrayList<>());
            } else if (args.length > 2 && args[1].equalsIgnoreCase("custom") && args[args.length-1].length() > 0) {
                List<String> list = new ArrayList<>();
                list.add("ALL");
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
