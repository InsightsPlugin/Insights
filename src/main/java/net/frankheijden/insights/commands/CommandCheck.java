package net.frankheijden.insights.commands;

import net.frankheijden.insights.utils.MessageUtils;
import org.bukkit.Chunk;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;

public class CommandCheck implements CommandExecutor, TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (player.hasPermission("insights.check")) {
                Chunk chunk = player.getLocation().getChunk();
                MessageUtils.sendMessage(player, "messages.check", "%tile_count%", NumberFormat.getIntegerInstance().format(chunk.getTileEntities().length), "%entity_count%", NumberFormat.getIntegerInstance().format(chunk.getEntities().length));
            } else {
                MessageUtils.sendMessage(player, "messages.no_permission");
            }
        } else {
            sender.sendMessage("This command cannot be executed from console!");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return Collections.emptyList();
    }
}
