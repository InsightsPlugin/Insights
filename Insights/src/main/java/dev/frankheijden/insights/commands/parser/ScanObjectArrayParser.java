package dev.frankheijden.insights.commands.parser;

import dev.frankheijden.insights.api.objects.wrappers.ScanObject;
import dev.frankheijden.insights.api.utils.Constants;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import dev.frankheijden.insights.api.utils.StringUtils;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserParameters;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;

public class ScanObjectArrayParser<C> implements ArgumentParser<C, ScanObject<?>[]>, SuggestionProvider<C> {
    protected static final Set<String> SUGGESTIONS = Stream.<Enum<?>>concat(
            Constants.BLOCKS.stream(),
            Constants.ENTITIES.stream()
    ).map(Enum::name).map(String::toLowerCase).collect(Collectors.toSet());

    public ScanObjectArrayParser(ParserParameters options) {
        //
    }

    @Override
    public ArgumentParseResult<ScanObject<?>[]> parse(CommandContext<C> ctx, CommandInput input) {
        try {
            // Find the index of the first item in the *actual* input
            // input.input() is the entire command
            int indexOf = input.input().indexOf(input.peekString());

            // Find the number of elements that need to be in the array
            int queueSize = input.input().substring(indexOf).split(" ").length;

            List<ScanObject<?>> items = new ArrayList<>(queueSize);
            for (var i = 0; i < (queueSize - 1); i++) {
                items.add(ScanObject.parse(input.peekString()));
                input.readString();
            }

            String last = input.peekString();
            if (!last.equalsIgnoreCase("-c") && !last.equalsIgnoreCase("--group-by-chunk")) {
                items.add(ScanObject.parse(last));
                input.readString();
            }

            return ArgumentParseResult.success(items.toArray(ScanObject[]::new));
        } catch (IllegalArgumentException ex) {
            return ArgumentParseResult.failure(new IllegalArgumentException(
                    "Invalid Material '" + input.peekString() + "'"
            ));
        }
    }

    @Override
    public CompletableFuture<? extends Iterable<? extends Suggestion>> suggestionsFuture(
            CommandContext<C> ctx,
            CommandInput input
    ) {
        return CompletableFuture.completedFuture(
                StringUtils.findThatStartsWith(SUGGESTIONS, input.peekString()).stream()
                        .map(Suggestion::suggestion)
                        .toList()
        );
    }
}
