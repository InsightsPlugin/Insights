package net.frankheijden.blocklimiter.commands;

import net.frankheijden.blocklimiter.BlockLimiter;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Objects;
import java.util.TreeSet;

public class CommandCheckworlds implements CommandExecutor {
    private BlockLimiter plugin;

    public CommandCheckworlds(BlockLimiter plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("blocklimiter.checkworlds")) {
            plugin.utils.sendMessage(sender, "messages.checkworlds.header");

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

                plugin.utils.sendMessage(sender, "messages.checkworlds.format", "%world%", worldName, "%entities%", String.valueOf(worldEntityCount), "%tiles%", String.valueOf(worldTileCount));

                totalTileCount = totalTileCount + worldTileCount;
                totalEntityCount = totalEntityCount + worldEntityCount;
            }

            if (totalTileCount > 0 || totalEntityCount > 0) {
                plugin.utils.sendMessage(sender, "messages.checkworlds.total", "%entities%", String.valueOf(totalEntityCount), "%tiles%", String.valueOf(totalTileCount));
            } else {
                plugin.utils.sendMessage(sender, "messages.checkworlds.none");
            }
            plugin.utils.sendMessage(sender, "messages.checkworlds.footer");
        } else {
            plugin.utils.sendMessage(sender, "messages.no_permission");
        }
        return true;
    }
}
