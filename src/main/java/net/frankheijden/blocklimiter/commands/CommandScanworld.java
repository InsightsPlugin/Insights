package net.frankheijden.blocklimiter.commands;

import net.frankheijden.blocklimiter.BlockLimiter;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
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
                            plugin.utils.sendMessage(sender, "messages.scanworld.both.no_entries");
                        }
                    } else {
                        plugin.utils.sendMessage(sender, "messages.checkworld.invalid_world");
                    }
                } else {
                    plugin.utils.sendMessage(sender, "messages.no_permission");
                }
            } else if (args.length == 2) {
                World world = Bukkit.getWorld(args[0]);
                if (world != null) {
                    if (args[1].equalsIgnoreCase("tile")) {
                        if (tilePerm) {
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
                                plugin.utils.sendMessage(sender, "messages.scanworld.tile.no_tiles");
                            }
                        } else {
                            plugin.utils.sendMessage(sender, "messages.no_permission");
                        }
                    } else if (args[1].equalsIgnoreCase("entity")) {
                        if (entityPerm) {
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
                                plugin.utils.sendMessage(sender, "messages.scanworld.entity.no_entities");
                            }
                        } else {
                            plugin.utils.sendMessage(sender, "messages.no_permission");
                        }
                    }
                } else {
                    plugin.utils.sendMessage(sender, "messages.checkworld.invalid_world");
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
