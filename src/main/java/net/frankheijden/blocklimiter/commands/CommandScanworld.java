package net.frankheijden.blocklimiter.commands;

import net.frankheijden.blocklimiter.BlockLimiter;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import java.util.Map;
import java.util.TreeMap;

public class CommandScanworld implements CommandExecutor {
    private BlockLimiter plugin;

    public CommandScanworld(BlockLimiter plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        boolean tilePerm = sender.hasPermission("blocklimiter.scanworld.tile");
        boolean entityPerm = sender.hasPermission("blocklimiter.scanworld.entity");

        if (tilePerm || entityPerm) {
            if (args.length == 2) {
                World world = Bukkit.getWorld(args[0]);
                if (world != null) {
                    if (args[1].equalsIgnoreCase("tile")) {
                        if (tilePerm) {
                            TreeMap<Material, Integer> tileTreeMap = new TreeMap<>();
                            for (Chunk chunk : world.getLoadedChunks()) {
                                for (BlockState bs : chunk.getTileEntities()) {
                                    tileTreeMap.merge(bs.getType(), 1, Integer::sum);
                                }
                            }

                            if (tileTreeMap.size() > 0) {
                                plugin.utils.sendMessage(sender, "messages.scanworld.tile.header");

                                int totalTileCount = 0;
                                for (Map.Entry<Material, Integer> entry : tileTreeMap.entrySet()) {
                                    String name = plugin.utils.capitalizeName(entry.getKey().name().toLowerCase());
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
}
