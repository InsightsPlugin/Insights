package net.frankheijden.insights.commands;

import net.frankheijden.insights.Insights;
import net.frankheijden.insights.tasks.LoadChunksTask;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class CommandCancelscan implements CommandExecutor, TabExecutor {
    private Insights plugin;

    public CommandCancelscan(Insights plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (plugin.getPlayerScanTasks().containsKey(player.getUniqueId())) {
                    LoadChunksTask loadChunksTask = plugin.getPlayerScanTasks().get(player.getUniqueId());
                    loadChunksTask.forceStop();
                    plugin.getUtils().sendMessage(sender, "messages.cancelscan.success");
                } else {
                    plugin.getUtils().sendMessage(sender, "messages.cancelscan.not_scanning");
                }
                return true;
            } else {
                sender.sendMessage("This command is not executable in the console.");
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return Collections.emptyList();
    }
}
