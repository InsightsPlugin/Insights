package dev.frankheijden.insights.commands;

import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.config.Messages;
import dev.frankheijden.insights.api.utils.ColorUtils;
import org.bukkit.command.CommandSender;

public class CommandInsights {

    private final InsightsPlugin plugin;

    public CommandInsights(InsightsPlugin plugin) {
        this.plugin = plugin;
    }

    @CommandMethod("insights|in")
    private void showBase(CommandSender sender) {
        sender.sendMessage(ColorUtils.colorize(
                "&8&l&m---------------=&r&8[ &b&lInsights&8 ]&l&m=----------------",
                "&b Plugin version: &a" + plugin.getDescription().getVersion(),
                "&b Plugin author: &7FrankHeijden#0099",
                "&b Plugin link: &7https://www.spigotmc.org/resources/56489/",
                "&b Wiki: &7https://github.com/InsightsPlugin/Insights/wiki",
                "&8&m-------------------------------------------------"
        ));
    }

    @CommandMethod("insights|in reload")
    @CommandPermission("insights.reload")
    private void reloadConfigurations(CommandSender sender) {
        plugin.reloadConfigs();
        plugin.getMessages().getMessage(Messages.Key.CONFIGS_RELOADED)
                .color()
                .sendTo(sender);
    }
}
