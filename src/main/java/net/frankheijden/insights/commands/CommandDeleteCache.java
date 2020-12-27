package net.frankheijden.insights.commands;

import net.frankheijden.insights.managers.CacheManager;
import net.frankheijden.insights.utils.MessageUtils;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class CommandDeleteCache implements CommandExecutor, TabExecutor {

    private static final CacheManager cacheManager = CacheManager.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command cannot be executed from console!");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("insights.deletecache")) {
            MessageUtils.sendMessage(player, "messages.no_permission");
            return true;
        }

        long deleted = cacheManager.newCacheLocation(player.getLocation())
                .getCache()
                .filter(cacheManager::deleteCache)
                .count();

        String path = "messages.deletecache." + (deleted > 0 ? "success" : "no_cache");
        MessageUtils.sendMessage(player, path);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
