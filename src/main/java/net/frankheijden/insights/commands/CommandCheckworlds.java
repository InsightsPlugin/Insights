package net.frankheijden.insights.commands;

import net.frankheijden.insights.utils.MessageUtils;
import org.bukkit.*;
import org.bukkit.command.*;

import java.text.NumberFormat;
import java.util.*;

public class CommandCheckworlds implements CommandExecutor, TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("insights.checkworlds")) {
            MessageUtils.sendMessage(sender, "messages.checkworlds.header");

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

                MessageUtils.sendMessage(sender, "messages.checkworlds.format", "%world%", worldName, "%entities%", NumberFormat.getIntegerInstance().format(worldEntityCount), "%tiles%", NumberFormat.getIntegerInstance().format(worldTileCount));

                totalTileCount = totalTileCount + worldTileCount;
                totalEntityCount = totalEntityCount + worldEntityCount;
            }

            if (totalTileCount > 0 || totalEntityCount > 0) {
                MessageUtils.sendMessage(sender, "messages.checkworlds.total", "%entities%", NumberFormat.getIntegerInstance().format(totalEntityCount), "%tiles%", NumberFormat.getIntegerInstance().format(totalTileCount));
            } else {
                MessageUtils.sendMessage(sender, "messages.checkworlds.none");
            }
            MessageUtils.sendMessage(sender, "messages.checkworlds.footer");
        } else {
            MessageUtils.sendMessage(sender, "messages.no_permission");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return Collections.emptyList();
    }
}
