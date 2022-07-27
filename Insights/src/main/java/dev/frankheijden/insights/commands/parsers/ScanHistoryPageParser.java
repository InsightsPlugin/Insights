package dev.frankheijden.insights.commands.parsers;

import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.config.Messages;
import dev.frankheijden.insights.api.utils.StringUtils;
import dev.frankheijden.insights.commands.CommandScanHistory;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

public class ScanHistoryPageParser implements ArgumentParser<CommandSender, CommandScanHistory.Page> {

    @Override
    public @NonNull ArgumentParseResult<CommandScanHistory.Page> parse(
            @NonNull CommandContext<CommandSender> cxt,
            @NonNull Queue<String> inputQueue
    ) {
        if (inputQueue.isEmpty()) {
            return ArgumentParseResult.failure(new NoInputProvidedException(
                    ScanHistoryPageParser.class,
                    cxt
            ));
        }

        try {
            var pageNumber = Integer.parseInt(inputQueue.peek());
            if (pageNumber <= 0) throw new NumberFormatException();

            inputQueue.poll();
            return ArgumentParseResult.success(new CommandScanHistory.Page(pageNumber - 1));
        } catch (NumberFormatException ex) {
            return ArgumentParseResult.failure(new IllegalArgumentException(
                    "Invalid Page '" + inputQueue.peek() + "'"
            ));
        }
    }

    @Override
    public boolean isContextFree() {
        return true;
    }

    @Override
    public @NonNull List<String> suggestions(@NonNull CommandContext<CommandSender> context, @NonNull String input) {
        if (context.getSender() instanceof Player) {
            var scanHistory = InsightsPlugin.getInstance().scanHistory();
            int pages = scanHistory.getHistory(((Player) context.getSender()).getUniqueId())
                    .map(Messages.PaginatedMessage::getPageAmount)
                    .orElse(0);

            List<String> suggestions = new ArrayList<>(pages);
            for (var i = 1; i <= pages; i++) {
                suggestions.add(String.valueOf(i));
            }

            return StringUtils.findThatStartsWith(suggestions, input);
        }

        return Collections.emptyList();
    }
}
