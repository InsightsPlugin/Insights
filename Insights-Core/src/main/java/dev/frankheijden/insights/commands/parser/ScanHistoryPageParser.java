package dev.frankheijden.insights.commands.parser;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.config.Messages;
import dev.frankheijden.insights.api.utils.StringUtils;
import dev.frankheijden.insights.commands.CommandScanHistory;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.bukkit.entity.Player;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserParameters;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;

public class ScanHistoryPageParser<C> implements ArgumentParser<C, CommandScanHistory.Page>, SuggestionProvider<C> {

    public ScanHistoryPageParser(ParserParameters options) {
        //
    }

    @Override
    public ArgumentParseResult<CommandScanHistory.Page> parse(CommandContext<C> ctx, CommandInput input) {
        try {
            var pageNumber = Integer.parseInt(input.peekString());
            if (pageNumber <= 0) throw new NumberFormatException();

            input.readString();
            return ArgumentParseResult.success(new CommandScanHistory.Page(pageNumber - 1));
        } catch (NumberFormatException ex) {
            return ArgumentParseResult.failure(new IllegalArgumentException(
                    "Invalid Page '" + input.peekString() + "'"
            ));
        }
    }

    @Override
    public CompletableFuture<? extends Iterable<? extends Suggestion>> suggestionsFuture(
            CommandContext<C> ctx,
            CommandInput input
    ) {
        return CompletableFuture.supplyAsync(() -> {
            if (ctx.sender() instanceof CommandSourceStack sourceStack
                    && sourceStack.getSender() instanceof Player player) {
                var scanHistory = InsightsPlugin.getInstance().getScanHistory();
                int pages = scanHistory.getHistory(player.getUniqueId())
                        .map(Messages.PaginatedMessage::getPageAmount)
                        .orElse(0);

                List<String> suggestions = new ArrayList<>(pages);
                for (var i = 1; i <= pages; i++) {
                    suggestions.add(String.valueOf(i));
                }

                return StringUtils.findThatStartsWith(suggestions, input.peekString()).stream()
                        .map(Suggestion::suggestion)
                        .toList();
            }

            return Collections.emptyList();
        });
    }
}
