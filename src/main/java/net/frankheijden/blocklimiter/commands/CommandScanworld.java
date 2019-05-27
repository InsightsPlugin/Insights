package net.frankheijden.blocklimiter.commands;

import net.frankheijden.blocklimiter.BlockLimiter;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.util.StringUtil;

import java.util.*;

public class CommandScanworld implements CommandExecutor, TabExecutor {
    private BlockLimiter plugin;

    public CommandScanworld(BlockLimiter plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        boolean tilePerm = sender.hasPermission("blocklimiter.scanworld.tile");
        boolean entityPerm = sender.hasPermission("blocklimiter.scanworld.entity");

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
                                plugin.utils.sendMessage(sender, "messages.scanworld.both.format", "%entry%", name, "%count%", String.valueOf(entry.getValue()));

                            }

                            plugin.utils.sendMessage(sender, "messages.scanworld.both.total", "%world%", world.getName(), "%entities%", String.valueOf(totalEntityCount), "%tiles%", String.valueOf(totalTileCount));
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
                                    plugin.utils.sendMessage(sender, "messages.scanworld.tile.format", "%tile%", name, "%count%", String.valueOf(entry.getValue()));

                                    totalTileCount = totalTileCount + entry.getValue();
                                }

                                plugin.utils.sendMessage(sender, "messages.scanworld.tile.total", "%world%", world.getName(), "%total_count%", String.valueOf(totalTileCount));
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
                                    plugin.utils.sendMessage(sender, "messages.scanworld.entity.format", "%entity%", name, "%count%", String.valueOf(entry.getValue()));

                                    totalEntityCount = totalEntityCount + entry.getValue();
                                }

                                plugin.utils.sendMessage(sender, "messages.scanworld.entity.total", "%world%", world.getName(), "%total_count%", String.valueOf(totalEntityCount));
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
            } /*else if (args.length == 3) {
                World world = Bukkit.getWorld(args[0]);
                if (args[1].equalsIgnoreCase("individual")) {
                    Material material = Material.getMaterial(args[2]);
                    EntityType entityType = plugin.utils.getEntityType(args[2]);
                    if (material != null) {
                        if (sender.hasPermission("blocklimiter.scanworld.individual. " + material.name())) {
                            if (world != null) {
                                Chunk[] chunks = world.getLoadedChunks();
                                plugin.utils.sendMessage(sender, "messages.scanworld.individual.start", "%entry%", plugin.utils.capitalizeName(material.name().toLowerCase()), "%chunks%", String.valueOf(chunks.length));

                                for (Chunk chunk : chunks) {
                                    if (!chunk.isLoaded()) {
                                        chunk.load();
                                    }


                                }

                                ChunkSnapshot[][] chunkSnapshots = plugin.utils.getChunkSnapshots(player.getLocation().getChunk(), radius);
                                ScanChunksTask task = new ScanChunksTask(plugin, chunkSnapshots, player, material);
                                task.setPriority(Thread.MIN_PRIORITY);
                                task.start();
                            } else {
                                plugin.utils.sendMessage(sender, "messages.scanworld.invalid_world");
                            }
                        } else {
                            plugin.utils.sendMessage(sender, "messages.no_permission");
                        }
                    } else if (entityType != null) {
                        if (sender.hasPermission("blocklimiter.scanworld.individual. " + entityType.name())) {
                            if (world != null) {
                                int entityCount = 0;

                                for (Chunk chunk : world.getLoadedChunks()) {
                                    for (Entity entity : chunk.getEntities()) {
                                        if (entity.getType() == entityType) {
                                            entityCount++;
                                        }
                                    }
                                }

                                plugin.utils.sendMessage(sender, "messages.scanworld.individual.total", "%entry%", plugin.utils.capitalizeName(entityType.name().toLowerCase()), "%count%", String.valueOf(entityCount));
                            } else {
                                plugin.utils.sendMessage(sender, "messages.scanworld.invalid_world");
                            }
                        } else {
                            plugin.utils.sendMessage(sender, "messages.scanworld.individual");
                        }
                    } else {
                        plugin.utils.sendMessage(sender, "messages.scanworld.individual.invalid_argument");
                    }
                    return true;
                } else {
                    return false;
                }
            }*/ else {
                return false;
            }
        } else {
            plugin.utils.sendMessage(sender, "messages.no_permission");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        boolean entityPerm = sender.hasPermission("blocklimiter.scanworld.entity");
        boolean tilePerm = sender.hasPermission("blocklimiter.scanworld.tile");
        if (args.length == 1) {
            if (entityPerm || tilePerm) {
                List<String> list = new ArrayList<>();
                for (World world : Bukkit.getWorlds()) {
                    list.add(world.getName());
                }
                return StringUtil.copyPartialMatches(args[0], list, new ArrayList<>());
            }
        } else if (args.length == 2) {
            List<String> list = new ArrayList<>();
            if (sender.hasPermission("blocklimiter.scanworld.entity")) {
                list.add("entity");
            }
            if (sender.hasPermission("blocklimiter.scanworld.tile")) {
                list.add("tile");
            }
            return StringUtil.copyPartialMatches(args[1], list, new ArrayList<>());
        }
        return Collections.emptyList();
    }
}
