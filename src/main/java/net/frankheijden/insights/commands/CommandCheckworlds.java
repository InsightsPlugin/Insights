package net.frankheijden.insights.commands;

import net.frankheijden.insights.Insights;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;

public class CommandCheckworlds implements CommandExecutor, TabExecutor {
    private Insights plugin;

    public CommandCheckworlds(Insights plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("insights.checkworlds")) {
            plugin.getUtils().sendMessage(sender, "messages.checkworlds.header");

            int totalTileCount = 0;
            int totalEntityCount = 0;

            TreeSet<String> worldTreeSet = new TreeSet<>();
            for (World world : Bukkit.getWorlds()) {
                worldTreeSet.add(world.getName());
            }

            for (String worldName : worldTreeSet) {
                World world = Bukkit.getWorld(worldName);
                int worldEntityCount = Objects.requireNonNull(world).getEntities().size();

                int worldTileCount = 0;
                for (Chunk chunk : world.getLoadedChunks()) {
                    worldTileCount = worldTileCount + chunk.getTileEntities().length;
                }

                plugin.getUtils().sendMessage(sender, "messages.checkworlds.format", "%world%", worldName, "%entities%", NumberFormat.getIntegerInstance().format(worldEntityCount), "%tiles%", NumberFormat.getIntegerInstance().format(worldTileCount));

                totalTileCount = totalTileCount + worldTileCount;
                totalEntityCount = totalEntityCount + worldEntityCount;
            }

            if (totalTileCount > 0 || totalEntityCount > 0) {
                plugin.getUtils().sendMessage(sender, "messages.checkworlds.total", "%entities%", NumberFormat.getIntegerInstance().format(totalEntityCount), "%tiles%", NumberFormat.getIntegerInstance().format(totalTileCount));
            } else {
                plugin.getUtils().sendMessage(sender, "messages.checkworlds.none");
            }
            plugin.getUtils().sendMessage(sender, "messages.checkworlds.footer");
        } else {
            plugin.getUtils().sendMessage(sender, "messages.no_permission");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return Collections.emptyList();
    }
}
