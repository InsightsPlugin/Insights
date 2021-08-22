package dev.frankheijden.insights.commands.parser;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import dev.frankheijden.insights.api.objects.wrappers.ScanObject;
import dev.frankheijden.insights.api.utils.Constants;
import dev.frankheijden.insights.api.utils.StringUtils;
import io.leangen.geantyref.TypeToken;
import org.bukkit.command.CommandSender;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ScanObjectArrayArgument extends CommandArgument<CommandSender, ScanObject<?>[]> {

    protected static final Set<String> SUGGESTIONS = Stream.<Enum<?>>concat(
            Constants.BLOCKS.stream(),
            Constants.ENTITIES.stream()
    ).map(Enum::name).map(String::toLowerCase).collect(Collectors.toSet());

    /**
     * Constructs a ScanObject[] argument.
     */
    public ScanObjectArrayArgument(
            boolean required,
            String name,
            BiFunction<CommandContext<CommandSender>, String, List<String>> suggestionsProvider,
            ArgumentDescription defaultDescription
    ) {
        super(
                required,
                name,
                new ScanObjectArrayParser(),
                "",
                new TypeToken<ScanObject<?>[]>() {},
                suggestionsProvider,
                defaultDescription
        );
    }

    public static final class ScanObjectArrayParser implements ArgumentParser<CommandSender, ScanObject<?>[]> {

        @Override
        public ArgumentParseResult<ScanObject<?>[]> parse(CommandContext<CommandSender> cxt, Queue<String> inputQueue) {
            if (inputQueue.isEmpty()) {
                return ArgumentParseResult.failure(new NoInputProvidedException(ScanObjectArrayParser.class, cxt));
            }

            try {
                int queueSize = inputQueue.size() - 1;
                List<ScanObject<?>> items = new ArrayList<>(queueSize);
                for (var i = 0; i < queueSize; i++) {
                    items.add(ScanObject.parse(inputQueue.peek()));
                    inputQueue.remove();
                }

                String last = inputQueue.peek();
                if (!last.equalsIgnoreCase("-c") && !last.equalsIgnoreCase("--group-by-chunk")) {
                    items.add(ScanObject.parse(last));
                    inputQueue.remove();
                }

                return ArgumentParseResult.success(items.toArray(ScanObject[]::new));
            } catch (IllegalArgumentException ex) {
                return ArgumentParseResult.failure(new IllegalArgumentException(
                        "Invalid Material '" + inputQueue.peek() + "'"
                ));
            }
        }

        @Override
        public boolean isContextFree() {
            return true;
        }

        @Override
        public List<String> suggestions(CommandContext<CommandSender> context, String input) {
            return StringUtils.findThatStartsWith(SUGGESTIONS, input);
        }
    }
}
