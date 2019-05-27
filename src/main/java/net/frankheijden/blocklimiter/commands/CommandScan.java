package net.frankheijden.blocklimiter.commands;

import net.frankheijden.blocklimiter.BlockLimiter;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;

public class CommandScan implements CommandExecutor, TabExecutor {
    private BlockLimiter plugin;

    public CommandScan(BlockLimiter plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (args.length == 0) {
                if (player.hasPermission("blocklimiter.scan.entity") && player.hasPermission("blocklimiter.scan.tile")) {
                    Chunk chunk = player.getLocation().getChunk();
                    int entityCount = chunk.getEntities().length;
                    int tileCount = chunk.getTileEntities().length;
                    if (entityCount > 1 || tileCount > 0) {
                        plugin.utils.sendMessage(player, "messages.scan.both.header");

                        TreeMap<String, Integer> entryTreeMap = new TreeMap<>();
                        for (Entity entity : chunk.getEntities()) {
                            entryTreeMap.merge(entity.getType().name(), 1, Integer::sum);
                        }
                        for (BlockState bs : chunk.getTileEntities()) {
                            entryTreeMap.merge(bs.getType().name(), 1, Integer::sum);
                        }
                        for (Map.Entry<String, Integer> entry : entryTreeMap.entrySet()) {
                            String name = plugin.utils.capitalizeName(entry.getKey().toLowerCase());
                            plugin.utils.sendMessage(player, "messages.scan.both.format", "%entry%", name, "%count%", String.valueOf(entry.getValue()));
                        }

                        plugin.utils.sendMessage(player, "messages.scan.both.total", "%entities%", String.valueOf(entityCount), "%tiles%", String.valueOf(tileCount));
                        plugin.utils.sendMessage(player, "messages.scan.both.footer");
                    } else {
                        plugin.utils.sendMessage(player, "messages.scan.both.no_entries");
                    }
                } else {
                    plugin.utils.sendMessage(player, "messages.no_permission");
                }
                return true;
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("entity")) {
                    if (player.hasPermission("blocklimiter.scan.entity")) {
                        int entityCount = player.getLocation().getChunk().getEntities().length;
                        if (entityCount > 1) {
                            plugin.utils.sendMessage(player, "messages.scan.entity.header");

                            TreeMap<String, Integer> entityTreeMap = new TreeMap<>();
                            for (Entity entity : player.getLocation().getChunk().getEntities()) {
                                entityTreeMap.merge(entity.getType().name(), 1, Integer::sum);
                            }
                            for (Map.Entry<String, Integer> entry : entityTreeMap.entrySet()) {
                                String name = plugin.utils.capitalizeName(entry.getKey().toLowerCase());
                                plugin.utils.sendMessage(player, "messages.scan.entity.format", "%entity%", name, "%count%", String.valueOf(entry.getValue()));
                            }

                            plugin.utils.sendMessage(player, "messages.scan.entity.total", "%total_count%", String.valueOf(entityCount));
                            plugin.utils.sendMessage(player, "messages.scan.entity.footer");
                        } else {
                            plugin.utils.sendMessage(player, "messages.scan.entity.no_entities");
                        }
                    } else {
                        plugin.utils.sendMessage(player, "messages.no_permission");
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("tile")) {
                    if (player.hasPermission("blocklimiter.scan.tile")) {
                        int tileCount = player.getLocation().getChunk().getTileEntities().length;
                        if (tileCount > 0) {
                            plugin.utils.sendMessage(player, "messages.scan.tile.header");

                            TreeMap<String, Integer> tileTreeMap = new TreeMap<>();
                            for (BlockState bs : player.getLocation().getChunk().getTileEntities()) {
                                tileTreeMap.merge(bs.getType().name(), 1, Integer::sum);
                            }

                            for (Map.Entry<String, Integer> entry : tileTreeMap.entrySet()) {
                                String name = plugin.utils.capitalizeName(entry.getKey().toLowerCase());
                                plugin.utils.sendMessage(player, "messages.scan.tile.format", "%tile%", name, "%count%", String.valueOf(entry.getValue()));
                            }

                            plugin.utils.sendMessage(player, "messages.scan.tile.total", "%total_count%", String.valueOf(tileCount));
                            plugin.utils.sendMessage(player, "messages.scan.tile.footer");
                        } else {
                            plugin.utils.sendMessage(player, "messages.scan.tile.no_tiles");
                        }
                    } else {
                        plugin.utils.sendMessage(player, "messages.no_permission");
                    }
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
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            if (sender.hasPermission("blocklimiter.scan.entity")) {
                list.add("entity");
            }
            if (sender.hasPermission("blocklimiter.scan.tile")) {
                list.add("tile");
            }
            return StringUtil.copyPartialMatches(args[0], list, new ArrayList<>());
        }
        return Collections.emptyList();
    }
}
