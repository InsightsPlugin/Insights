package dev.frankheijden.insights.commands;

import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.config.Messages;
import org.bukkit.command.CommandSender;

public class CommandReload {

    private final InsightsPlugin plugin;

    public CommandReload(InsightsPlugin plugin) {
        this.plugin = plugin;
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
