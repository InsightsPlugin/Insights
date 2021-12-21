package dev.frankheijden.insights.commands.parser;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.config.limits.Limit;
import io.leangen.geantyref.TypeToken;
import org.bukkit.command.CommandSender;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;

public class LimitArgument extends CommandArgument<CommandSender, Limit> {

    /**
     * Constructs a Limit argument.
     */
    public LimitArgument(
            boolean required,
            String name,
            BiFunction<CommandContext<CommandSender>, String, List<String>> suggestionsProvider,
            ArgumentDescription defaultDescription
    ) {
        super(
                required,
                name,
                new LimitParser(),
                "",
                new TypeToken<Limit>() {},
                suggestionsProvider,
                defaultDescription
        );
    }

    public static final class LimitParser implements ArgumentParser<CommandSender, Limit> {

        @Override
        public ArgumentParseResult<Limit> parse(
                CommandContext<CommandSender> cxt,
                Queue<String> inputQueue
        ) {
            if (inputQueue.isEmpty()) {
                return ArgumentParseResult.failure(new NoInputProvidedException(
                        LimitParser.class,
                        cxt
                ));
            }

            var fileName = inputQueue.peek();
            var limit = InsightsPlugin.getInstance().getLimits().getLimitByFileName(fileName);
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
        public List<String> suggestions(CommandContext<CommandSender> context, String input) {
            return new ArrayList<>(InsightsPlugin.getInstance().getLimits().getLimitFileNames());
        }
    }
}
