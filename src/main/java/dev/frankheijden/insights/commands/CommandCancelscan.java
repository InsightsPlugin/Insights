package dev.frankheijden.insights.commands;

import dev.frankheijden.insights.managers.ScanManager;
import dev.frankheijden.insights.tasks.LoadChunksTask;
import dev.frankheijden.insights.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class CommandCancelscan implements CommandExecutor, TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (ScanManager.getInstance().isScanning(player)) {
                    LoadChunksTask loadChunksTask = ScanManager.getInstance().getTask(player);
                    loadChunksTask.forceStop();
                    MessageUtils.sendMessage(sender, "messages.cancelscan.success");
                } else {
                    MessageUtils.sendMessage(sender, "messages.cancelscan.not_scanning");
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
