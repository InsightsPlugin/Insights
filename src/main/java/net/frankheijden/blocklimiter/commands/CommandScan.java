package net.frankheijden.blocklimiter.commands;

import net.frankheijden.blocklimiter.BlockLimiter;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.TreeMap;

public class CommandScan implements CommandExecutor {
    private BlockLimiter plugin;

    public CommandScan(BlockLimiter plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (args.length == 1) {
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

                            TreeMap<Material, Integer> tileTreeMap = new TreeMap<>();
                            for (BlockState bs : player.getLocation().getChunk().getTileEntities()) {
                                tileTreeMap.merge(bs.getType(), 1, Integer::sum);
                            }

                            for (Map.Entry<Material, Integer> entry : tileTreeMap.entrySet()) {
                                String name = plugin.utils.capitalizeName(entry.getKey().name().toLowerCase());
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
}
