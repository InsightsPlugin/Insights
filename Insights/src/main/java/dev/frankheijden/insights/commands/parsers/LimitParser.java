package dev.frankheijden.insights.commands.parsers;

import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.config.limits.Limit;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class LimitParser implements ArgumentParser<CommandSender, Limit> {

    @Override
    public @NonNull ArgumentParseResult<Limit> parse(
            @NonNull CommandContext<CommandSender> cxt,
            Queue<String> inputQueue
    ) {
        if (inputQueue.isEmpty()) {
            return ArgumentParseResult.failure(new NoInputProvidedException(
                    LimitParser.class,
                    cxt
            ));
        }

        var fileName = inputQueue.peek();
        var limit = InsightsPlugin.getInstance().limits().getLimitByFileName(fileName);
        if (limit.isEmpty()) {
            return ArgumentParseResult.failure(new IllegalArgumentException(
                    "Invalid limit file name '" + fileName + "'"
            ));
        }

        inputQueue.poll();
        return ArgumentParseResult.success(limit.get());
    }

    @Override
    public boolean isContextFree() {
        return true;
    }

    @Override
    public @NonNull List<String> suggestions(@NonNull CommandContext<CommandSender> context, @NonNull String input) {
        return new ArrayList<>(InsightsPlugin.getInstance().limits().getLimitFileNames());
    }
}
