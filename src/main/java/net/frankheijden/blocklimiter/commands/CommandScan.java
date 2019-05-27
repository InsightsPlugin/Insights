package net.frankheijden.blocklimiter.commands;

import net.frankheijden.blocklimiter.BlockLimiter;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.EntityType;
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

                        for (Map.Entry<String, Integer> entry : plugin.utils.getEntitiesAndTilesInChunk(chunk)) {
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

                            for (Map.Entry<String, Integer> entry : plugin.utils.getEntitiesInChunk(player.getLocation().getChunk())) {
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

                            for (Map.Entry<String, Integer> entry : plugin.utils.getTilesInChunk(player.getLocation().getChunk())) {
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
            } else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("individual")) {
                    Material material = Material.getMaterial(args[1]);
                    EntityType entityType = plugin.utils.getEntityType(args[1]);
                    if (material != null) {
                        if (player.hasPermission("blocklimiter.scan.individual. " + material.name())) {
                            int materialCount = plugin.utils.getAmountInChunk(player.getLocation().getChunk(), material);
                            plugin.utils.sendMessage(player, "messages.scan.individual.total", "%entry%", plugin.utils.capitalizeName(material.name().toLowerCase()), "%count%", String.valueOf(materialCount));
                        } else {
                            plugin.utils.sendMessage(player, "messages.no_permission");
                        }
                    } else if (entityType != null) {
                        if (player.hasPermission("blocklimiter.scan.individual. " + entityType.name())) {
                            int entityCount = plugin.utils.getAmountInChunk(player.getLocation().getChunk(), entityType);
                            plugin.utils.sendMessage(player, "messages.scan.individual.total", "%entry%", plugin.utils.capitalizeName(entityType.name().toLowerCase()), "%count%", String.valueOf(entityCount));
                        } else {
                            plugin.utils.sendMessage(player, "messages.scan.individual");
                        }
                    } else {
                        plugin.utils.sendMessage(player, "messages.scan.individual.invalid_argument");
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
            for (Material material : Material.values()) {
                if (material.isBlock()) {
                    if (sender.hasPermission("blocklimiter.scan.individual." + material.name())) {
                        list.add("individual");
                        break;
                    }
                }
            }
            if (!list.contains("individual")) {
                for (EntityType entityType : EntityType.values()) {
                    if (sender.hasPermission("blocklimiter.scan.individual." + entityType.name())) {
                        list.add("individual");
                        break;
                    }
                }
            }
            if (sender.hasPermission("blocklimiter.scan.tile")) {
                list.add("tile");
            }
            return StringUtil.copyPartialMatches(args[0], list, new ArrayList<>());
        } else if (args.length == 2 && args[0].equalsIgnoreCase("individual") && args[1].length() > 0) {
            List<String> list = new ArrayList<>();
            for (Material material : Material.values()) {
                if (material.isBlock()) {
                    if (sender.hasPermission("blocklimiter.scan.individual." + material.name())) {
                        list.add(material.name());
                    }
                }
            }
            for (EntityType entityType : EntityType.values()) {
                if (sender.hasPermission("blocklimiter.scan.individual." + entityType.name())) {
                    list.add(entityType.name());
                }
            }
            return StringUtil.copyPartialMatches(args[1], list, new ArrayList<>());
        }
        return Collections.emptyList();
    }
}
