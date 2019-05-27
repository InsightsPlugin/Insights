package net.frankheijden.blocklimiter.commands;

import net.frankheijden.blocklimiter.BlockLimiter;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;

public class CommandCheck implements CommandExecutor, TabExecutor {
    private BlockLimiter plugin;

    public CommandCheck(BlockLimiter plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (player.hasPermission("blocklimiter.check")) {
                Chunk chunk = player.getLocation().getChunk();
                plugin.utils.sendMessage(player, "messages.check", "%tile_count%", NumberFormat.getIntegerInstance().format(chunk.getTileEntities().length), "%entity_count%", NumberFormat.getIntegerInstance().format(chunk.getEntities().length));
            } else {
                plugin.utils.sendMessage(player, "messages.no_permission");
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
