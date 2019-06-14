package net.frankheijden.insights.commands;

import net.frankheijden.insights.Insights;
import net.frankheijden.insights.objects.ChunkLocation;
import net.frankheijden.insights.tasks.ScanTask;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.text.NumberFormat;
import java.util.*;

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
                    Chunk chunk = player.getLocation().getChunk();
                    int entityCount = chunk.getEntities().length;
                    int tileCount = chunk.getTileEntities().length;
                    if (entityCount > 1 || tileCount > 0) {
                        plugin.utils.sendMessage(player, "messages.scan.both.header");

                        for (Map.Entry<String, Integer> entry : plugin.utils.getEntitiesAndTilesInChunk(chunk)) {
                            String name = plugin.utils.capitalizeName(entry.getKey().toLowerCase());
                            plugin.utils.sendMessage(player, "messages.scan.both.format", "%entry%", name, "%count%", NumberFormat.getIntegerInstance().format(entry.getValue()));
                        }

                        plugin.utils.sendMessage(player, "messages.scan.both.total", "%entities%", NumberFormat.getIntegerInstance().format(entityCount), "%tiles%", NumberFormat.getIntegerInstance().format(tileCount));
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
                    if (player.hasPermission("insights.scan.entity")) {
                        int entityCount = player.getLocation().getChunk().getEntities().length;
                        if (entityCount > 1) {
                            plugin.utils.sendMessage(player, "messages.scan.entity.header");

                            for (Map.Entry<String, Integer> entry : plugin.utils.getEntitiesInChunk(player.getLocation().getChunk())) {
                                String name = plugin.utils.capitalizeName(entry.getKey().toLowerCase());
                                plugin.utils.sendMessage(player, "messages.scan.entity.format", "%entity%", name, "%count%", NumberFormat.getIntegerInstance().format(entry.getValue()));
                            }

                            plugin.utils.sendMessage(player, "messages.scan.entity.total", "%total_count%", NumberFormat.getIntegerInstance().format(entityCount));
                            plugin.utils.sendMessage(player, "messages.scan.entity.footer");
                        } else {
                            plugin.utils.sendMessage(player, "messages.scan.entity.no_entities");
                        }
                    } else {
                        plugin.utils.sendMessage(player, "messages.no_permission");
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("tile")) {
                    if (player.hasPermission("insights.scan.tile")) {
                        int tileCount = player.getLocation().getChunk().getTileEntities().length;
                        if (tileCount > 0) {
                            plugin.utils.sendMessage(player, "messages.scan.tile.header");

                            for (Map.Entry<String, Integer> entry : plugin.utils.getTilesInChunk(player.getLocation().getChunk())) {
                                String name = plugin.utils.capitalizeName(entry.getKey().toLowerCase());
                                plugin.utils.sendMessage(player, "messages.scan.tile.format", "%tile%", name, "%count%", NumberFormat.getIntegerInstance().format(entry.getValue()));
                            }

                            plugin.utils.sendMessage(player, "messages.scan.tile.total", "%total_count%", NumberFormat.getIntegerInstance().format(tileCount));
                            plugin.utils.sendMessage(player, "messages.scan.tile.footer");
                        } else {
                            plugin.utils.sendMessage(player, "messages.scan.tile.no_tiles");
                        }
                    } else {
                        plugin.utils.sendMessage(player, "messages.no_permission");
                    }
                    return true;
                }
            } else {
                if (args[0].equalsIgnoreCase("custom")) {
                    long startTime = System.currentTimeMillis();

                    ArrayList<Material> materials = new ArrayList<>();
                    ArrayList<EntityType> entityTypes = new ArrayList<>();
                    boolean isAll = false;
                    for (int i = 1; i < args.length; i++) {
                        Material material = Material.getMaterial(args[i]);
                        EntityType entityType = plugin.utils.getEntityType(args[i]);
                        if (material != null) {
                            if (sender.hasPermission("insights.scan.custom." + material.name())) {
                                materials.add(material);
                            } else {
                                plugin.utils.sendMessage(sender, "messages.no_permission");
                                return true;
                            }
                        } else if (entityType != null) {
                            if (sender.hasPermission("insights.scan.custom." + entityType.name())) {
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
                            plugin.utils.sendMessage(sender, "messages.scan.custom.invalid_argument", "%argument%", args[i]);
                            return true;
                        }
                    }

                    if (materials.isEmpty() && entityTypes.isEmpty() && !isAll) return true;

                    List<ChunkLocation> chunkLocations = Collections.singletonList(new ChunkLocation(player.getLocation().getChunk()));

                    ScanTask task = new ScanTask(plugin, player.getWorld(), player, "messages.scan.custom", chunkLocations, materials, entityTypes);
                    task.start(startTime);
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
                List<String> list = Arrays.asList("custom", "entity", "tile");
                return StringUtil.copyPartialMatches(args[0], list, new ArrayList<>());
            } else if (args.length > 1 && args[0].equalsIgnoreCase("custom") && args[args.length-1].length() > 0) {
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
