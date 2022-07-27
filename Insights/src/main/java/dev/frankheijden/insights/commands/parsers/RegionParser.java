package dev.frankheijden.insights.commands.parsers;

import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.region.Region;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

public class RegionParser implements ArgumentParser<CommandSender, Region> {

    @Override
    public @NonNull ArgumentParseResult<Region> parse(
            @NonNull CommandContext<CommandSender> cxt,
            Queue<String> inputQueue
    ) {
        if (inputQueue.isEmpty()) {
            return ArgumentParseResult.failure(new NoInputProvidedException(
                    LimitParser.class,
                    cxt
            ));
        }

        if (!(cxt.getSender() instanceof Player player)) {
            return ArgumentParseResult.failure(new IllegalArgumentException(
                    "You cannot execute this command from the console"
            ));
        }

        var regionName = inputQueue.peek();
        var regionOptional = regionsAt(player).stream()
                .filter(region -> region.name().equals(regionName))
                .findAny();
        if (regionOptional.isEmpty()) {
            return ArgumentParseResult.failure(new IllegalArgumentException(
                    "Invalid region name '" + regionName + "'"
            ));
        }

        inputQueue.poll();
        return ArgumentParseResult.success(regionOptional.get());
    }

    @Override
    public boolean isContextFree() {
        return false;
    }

    @Override
    public @NonNull List<String> suggestions(@NonNull CommandContext<CommandSender> context, @NonNull String input) {
        if (context.getSender() instanceof Player player) {
            return regionsAt(player).stream()
                    .map(Region::name)
                    .toList();
        }
        return Collections.emptyList();
    }

    private List<Region> regionsAt(Player player) {
        return InsightsPlugin.getInstance().regionManager().regionsAt(player.getLocation());
    }
}
