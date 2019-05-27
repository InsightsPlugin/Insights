package net.frankheijden.blocklimiter.commands;

import net.frankheijden.blocklimiter.BlockLimiter;
import net.frankheijden.blocklimiter.tasks.ScanChunksTask;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;

public class CommandScanradius implements CommandExecutor, TabExecutor {
    private BlockLimiter plugin;

    public CommandScanradius(BlockLimiter plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        boolean tilePerm = sender.hasPermission("blocklimiter.scanradius.tile");
        boolean entityPerm = sender.hasPermission("blocklimiter.scanradius.entity");

        if (tilePerm || entityPerm) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                if (args.length == 1) {
                    if (entityPerm && tilePerm) {
                        if (args[0].matches("-?(0|[1-9]\\d*)")) {
                            int radius = Integer.valueOf(args[0]);
                            if (radius >= 1 && radius <= 25) {
                                World world = player.getWorld();
                                int x = player.getLocation().getChunk().getX();
                                int z = player.getLocation().getChunk().getZ();

                                TreeMap<String, Integer> entryTreeMap = new TreeMap<>();
                                int totalEntityCount = 0;
                                int totalTileCount = 0;
                                for (int xc = x-radius; xc <= x+radius; xc++) {
                                    for (int zc = z-radius; zc <= z+radius; zc++) {
                                        Chunk chunk = world.getChunkAt(xc, zc);
                                        if (!chunk.isLoaded()) {
                                            chunk.load();
                                        }

                                        for (Entity entity : chunk.getEntities()) {
                                            entryTreeMap.merge(entity.getType().name(), 1, Integer::sum);
                                            totalEntityCount++;
                                        }
                                        for (BlockState bs : chunk.getTileEntities()) {
                                            entryTreeMap.merge(bs.getType().name(), 1, Integer::sum);
                                            totalTileCount++;
                                        }
                                    }
                                }

                                if (entryTreeMap.size() > 0) {
                                    plugin.utils.sendMessage(sender, "messages.scanradius.both.header");

                                    for (Map.Entry<String, Integer> entry : entryTreeMap.entrySet()) {
                                        String name = plugin.utils.capitalizeName(entry.getKey().toLowerCase());
                                        plugin.utils.sendMessage(sender, "messages.scanradius.both.format", "%entry%", name, "%count%", String.valueOf(entry.getValue()));

                                        totalEntityCount = totalEntityCount + entry.getValue();
                                    }

                                    plugin.utils.sendMessage(sender, "messages.scanradius.both.total", "%entities%", String.valueOf(totalEntityCount), "%tiles%", String.valueOf(totalTileCount));
                                    plugin.utils.sendMessage(sender, "messages.scanradius.both.footer");
                                } else {
                                    plugin.utils.sendMessage(sender, "messages.scanradius.both.no_entries");
                                }
                            } else {
                                plugin.utils.sendMessage(sender, "messages.scanradius.invalid_radius");
                            }
                        } else {
                            plugin.utils.sendMessage(sender, "messages.scanradius.invalid_number");
                        }
                    } else {
                        plugin.utils.sendMessage(sender, "messages.no_permission");
                    }
                    return true;
                } else if (args.length == 2) {
                    if (args[0].matches("-?(0|[1-9]\\d*)")) {
                        int radius = Integer.valueOf(args[0]);
                        if (radius >= 1 && radius <= 25) {

                            if (args[1].equalsIgnoreCase("entity")) {
                                if (entityPerm) {
                                    World world = player.getWorld();
                                    int x = player.getLocation().getChunk().getX();
                                    int z = player.getLocation().getChunk().getZ();

                                    TreeMap<String, Integer> entityTreeMap = new TreeMap<>();
                                    for (int xc = x-radius; xc <= x+radius; xc++) {
                                        for (int zc = z-radius; zc <= z+radius; zc++) {
                                            Chunk chunk = world.getChunkAt(xc, zc);
                                            if (!chunk.isLoaded()) {
                                                chunk.load();
                                            }

                                            for (Entity entity : chunk.getEntities()) {
                                                entityTreeMap.merge(entity.getType().name(), 1, Integer::sum);
                                            }
                                        }
                                    }

                                    if (entityTreeMap.size() > 0) {
                                        plugin.utils.sendMessage(sender, "messages.scanradius.entity.header");

                                        int totalEntityCount = 0;
                                        for (Map.Entry<String, Integer> entry : entityTreeMap.entrySet()) {
                                            String name = plugin.utils.capitalizeName(entry.getKey().toLowerCase());
                                            plugin.utils.sendMessage(sender, "messages.scanradius.entity.format", "%entity%", name, "%count%", String.valueOf(entry.getValue()));

                                            totalEntityCount = totalEntityCount + entry.getValue();
                                        }

                                        plugin.utils.sendMessage(sender, "messages.scanradius.entity.total", "%total_count%", String.valueOf(totalEntityCount));
                                        plugin.utils.sendMessage(sender, "messages.scanradius.entity.footer");
                                    } else {
                                        plugin.utils.sendMessage(sender, "messages.scanradius.entity.no_entities");
                                    }
                                } else {
                                    plugin.utils.sendMessage(sender, "messages.no_permission");
                                }
                            } else if (args[1].equalsIgnoreCase("tile")) {
                                if (tilePerm) {
                                    World world = player.getWorld();
                                    int x = player.getLocation().getChunk().getX();
                                    int z = player.getLocation().getChunk().getZ();

                                    TreeMap<String, Integer> tileTreeMap = new TreeMap<>();
                                    for (int xc = x-radius; xc <= x+radius; xc++) {
                                        for (int zc = z-radius; zc <= z+radius; zc++) {
                                            Chunk chunk = world.getChunkAt(xc, zc);
                                            if (!chunk.isLoaded()) {
                                                chunk.load();
                                            }

                                            for (BlockState bs : chunk.getTileEntities()) {
                                                tileTreeMap.merge(bs.getType().name(), 1, Integer::sum);
                                            }
                                        }
                                    }

                                    if (tileTreeMap.size() > 0) {
                                        plugin.utils.sendMessage(sender, "messages.scanradius.tile.header");

                                        int totalTileCount = 0;
                                        for (Map.Entry<String, Integer> entry : tileTreeMap.entrySet()) {
                                            String name = plugin.utils.capitalizeName(entry.getKey().toLowerCase());
                                            plugin.utils.sendMessage(sender, "messages.scanradius.tile.format", "%tile%", name, "%count%", String.valueOf(entry.getValue()));

                                            totalTileCount = totalTileCount + entry.getValue();
                                        }

                                        plugin.utils.sendMessage(sender, "messages.scanradius.tile.total", "%total_count%", String.valueOf(totalTileCount));
                                        plugin.utils.sendMessage(sender, "messages.scanradius.tile.footer");
                                    } else {
                                        plugin.utils.sendMessage(sender, "messages.scanradius.tile.no_tiles");
                                    }
                                } else {
                                    plugin.utils.sendMessage(sender, "messages.no_permission");
                                }
                            }
                        } else {
                            plugin.utils.sendMessage(sender, "messages.scanradius.invalid_radius");
                        }
                    } else {
                        plugin.utils.sendMessage(sender, "messages.scanradius.invalid_number");
                    }
                } else if (args.length == 3) {
                    if (args[0].matches("-?(0|[1-9]\\d*)")) {
                        int radius = Integer.valueOf(args[0]);
                        if (radius >= 1 && radius <= 25) {
                            if (args[1].equalsIgnoreCase("individual")) {
                                Material material = Material.getMaterial(args[2]);
                                EntityType entityType = plugin.utils.getEntityType(args[2]);
                                if (material != null) {
                                    if (player.hasPermission("blocklimiter.scanradius.individual. " + material.name())) {
                                        plugin.utils.sendMessage(player, "messages.scanradius.individual.start", "%entry%", plugin.utils.capitalizeName(material.name().toLowerCase()), "%chunks%", String.valueOf((radius * 2 + 1) * (radius * 2 + 1)));

                                        ChunkSnapshot[][] chunkSnapshots = plugin.utils.getChunkSnapshots(player.getLocation().getChunk(), radius);
                                        ScanChunksTask task = new ScanChunksTask(plugin, chunkSnapshots, player, material);
                                        task.setPriority(Thread.MIN_PRIORITY);
                                        task.start();

                                    } else {
                                        plugin.utils.sendMessage(player, "messages.no_permission");
                                    }
                                } else if (entityType != null) {
                                    if (player.hasPermission("blocklimiter.scanradius.individual. " + entityType.name())) {
                                        int entityCount = 0;

                                        World world = player.getWorld();
                                        int x = player.getLocation().getChunk().getX();
                                        int z = player.getLocation().getChunk().getZ();
                                        for (int xc = x-radius; xc <= x+radius; xc++) {
                                            for (int zc = z - radius; zc <= z + radius; zc++) {
                                                Chunk chunk = world.getChunkAt(xc, zc);
                                                if (!chunk.isLoaded()) {
                                                    chunk.load();
                                                }

                                                entityCount = entityCount + plugin.utils.getAmountInChunk(chunk, entityType);
                                            }
                                        }

                                        plugin.utils.sendMessage(player, "messages.scanradius.individual.total", "%entry%", plugin.utils.capitalizeName(entityType.name().toLowerCase()), "%count%", String.valueOf(entityCount));
                                    } else {
                                        plugin.utils.sendMessage(player, "messages.scanradius.individual");
                                    }
                                } else {
                                    plugin.utils.sendMessage(player, "messages.scanradius.individual.invalid_argument");
                                }
                                return true;
                            } else {
                                return false;
                            }
                        } else {
                            plugin.utils.sendMessage(sender, "messages.scanradius.invalid_radius");
                        }
                    } else {
                        plugin.utils.sendMessage(sender, "messages.scanradius.invalid_number");
                    }
                } else {
                    return false;
                }
            } else {
                sender.sendMessage("This command cannot be executed from console!");
            }
        } else {
            plugin.utils.sendMessage(sender, "messages.no_permission");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 2) {
            List<String> list = new ArrayList<>();
            if (sender.hasPermission("blocklimiter.scanradius.entity")) {
                list.add("entity");
            }
            for (Material material : Material.values()) {
                if (material.isBlock()) {
                    if (sender.hasPermission("blocklimiter.scanradius.individual." + material.name())) {
                        list.add("individual");
                        break;
                    }
                }
            }
            if (!list.contains("individual")) {
                for (EntityType entityType : EntityType.values()) {
                    if (sender.hasPermission("blocklimiter.scanradius.individual." + entityType.name())) {
                        list.add("individual");
                        break;
                    }
                }
            }
            if (sender.hasPermission("blocklimiter.scanradius.tile")) {
                list.add("tile");
            }
            return StringUtil.copyPartialMatches(args[1], list, new ArrayList<>());
        } else if (args.length == 3 && args[1].equalsIgnoreCase("individual") && args[2].length() > 0) {
            List<String> list = new ArrayList<>();
            for (Material material : Material.values()) {
                if (material.isBlock()) {
                    if (sender.hasPermission("blocklimiter.scanradius.individual." + material.name())) {
                        list.add(material.name());
                    }
                }
            }
            for (EntityType entityType : EntityType.values()) {
                if (sender.hasPermission("blocklimiter.scanradius.individual." + entityType.name())) {
                    list.add(entityType.name());
                }
            }
            return StringUtil.copyPartialMatches(args[2], list, new ArrayList<>());
        }
        return Collections.emptyList();
    }
}
