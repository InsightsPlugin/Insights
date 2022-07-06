package dev.frankheijden.insights.commands;

import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import dev.frankheijden.insights.Insights;
import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.commands.InsightsCommand;
import dev.frankheijden.insights.api.config.Messages;
import dev.frankheijden.insights.api.utils.ColorUtils;
import dev.frankheijden.insights.api.utils.StringUtils;
import dev.frankheijden.insights.concurrent.ContainerExecutorService;
import org.bukkit.command.CommandSender;

@CommandMethod("insights|in")
public class CommandInsights extends InsightsCommand {

    public CommandInsights(InsightsPlugin plugin) {
        super(plugin);
    }

    @CommandMethod("")
    @CommandPermission("insights.info")
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

    @CommandMethod("reload")
    @CommandPermission("insights.reload")
    private void reloadConfigurations(CommandSender sender) {
        plugin.reloadConfigs();
        plugin.reload();
        plugin.messages().getMessage(Messages.Key.CONFIGS_RELOADED).sendTo(sender);
    }

    @CommandMethod("stats")
    @CommandPermission("insights.stats")
    private void displayStatistics(CommandSender sender) {
        ContainerExecutorService executor = ((Insights) plugin).executor();
        plugin.messages().getMessage(Messages.Key.STATS).addTemplates(
                Messages.tagOf("chunks_scanned", StringUtils.pretty(executor.getCompletedTaskCount())),
                Messages.tagOf(
                        "blocks_scanned",
                        StringUtils.pretty(plugin.metricsManager().getTotalBlocksScanned().sum())
                ),
                Messages.tagOf("queue_size", StringUtils.pretty(executor.getQueueSize()))
        ).sendTo(sender);
    }
}
