package dev.frankheijden.insights.commands.parser;

import java.util.concurrent.CompletableFuture;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserParameters;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;

public class WorldParser<C> implements ArgumentParser<C, World>, SuggestionProvider<C> {
    public WorldParser(ParserParameters options) {
        //
    }

    @Override
    public ArgumentParseResult<World> parse(CommandContext<C> ctx, CommandInput input) {
        var world = Bukkit.getWorld(input.peekString());
        if (world == null) {
            return ArgumentParseResult.failure(new IllegalArgumentException(
                    "Invalid World '" + input.peekString() + "'"
            ));
        }

        input.readString();
        return ArgumentParseResult.success(world);
    }

    @Override
    public CompletableFuture<? extends Iterable<? extends Suggestion>> suggestionsFuture(
            CommandContext<C> ctx,
            CommandInput input
    ) {
        return CompletableFuture.completedFuture(
                Bukkit.getWorlds()
                        .stream()
                        .map(world -> Suggestion.suggestion(world.getName()))
                        .toList()
        );
    }
}
