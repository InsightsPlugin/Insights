package dev.frankheijden.insights.commands.parser;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.config.limits.Limit;
import java.util.concurrent.CompletableFuture;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserParameters;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;

public class LimitParser<C> implements ArgumentParser<C, Limit>, SuggestionProvider<C> {

    public LimitParser(ParserParameters options) {
        //
    }

    @Override
    public ArgumentParseResult<Limit> parse(CommandContext<C> ctx, CommandInput input) {
        var fileName = input.peekString();
        var limit = InsightsPlugin.getInstance().getLimits().getLimitByFileName(fileName);
        if (limit.isEmpty()) {
            return ArgumentParseResult.failure(new IllegalArgumentException(
                    "Invalid limit file name '" + fileName + "'"
            ));
        }

        input.readString();
        return ArgumentParseResult.success(limit.get());
    }


    @Override
    public CompletableFuture<? extends Iterable<? extends Suggestion>> suggestionsFuture(
            CommandContext<C> ctx,
            CommandInput input
    ) {
        return CompletableFuture.completedFuture(
                InsightsPlugin.getInstance()
                        .getLimits()
                        .getLimitFileNames()
                        .stream()
                        .map(Suggestion::suggestion)
                        .toList()
        );
    }
}
