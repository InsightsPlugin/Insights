package dev.frankheijden.insights.commands.parser;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import dev.frankheijden.insights.api.objects.wrappers.ScanObject;
import dev.frankheijden.insights.api.utils.MaterialUtils;
import dev.frankheijden.insights.api.utils.StringUtils;
import io.leangen.geantyref.TypeToken;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ScanObjectArrayArgument extends CommandArgument<CommandSender, ScanObject<?>[]> {

    protected static final Set<String> SUGGESTIONS = Stream.<Enum<?>>concat(
            MaterialUtils.BLOCKS.stream(),
            MaterialUtils.ENTITIES.stream()
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
                ScanObject<?>[] items = new ScanObject[inputQueue.size()];
                for (int i = 0; i < items.length; i++) {
                    String str = inputQueue.peek().toUpperCase();
                    try {
                        items[i] = ScanObject.of(Material.valueOf(str));
                    } catch (IllegalArgumentException ex) {
                        items[i] = ScanObject.of(EntityType.valueOf(str));
                    }
                    inputQueue.remove();
                }
                return ArgumentParseResult.success(items);
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
