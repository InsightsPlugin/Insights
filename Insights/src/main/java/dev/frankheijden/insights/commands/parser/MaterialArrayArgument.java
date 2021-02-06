package dev.frankheijden.insights.commands.parser;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import dev.frankheijden.insights.api.utils.MaterialUtils;
import dev.frankheijden.insights.api.utils.StringUtils;
import io.leangen.geantyref.TypeToken;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class MaterialArrayArgument extends CommandArgument<CommandSender, Material[]> {

    protected static final Set<String> SUGGESTION_BLOCKS = MaterialUtils.BLOCKS.stream()
            .map(Enum::name)
            .map(String::toLowerCase)
            .collect(Collectors.toSet());

    /**
     * Constructs a Material[] argument.
     */
    public MaterialArrayArgument(
            boolean required,
            String name,
            BiFunction<CommandContext<CommandSender>, String, List<String>> suggestionsProvider,
            ArgumentDescription defaultDescription
    ) {
        super(
                required,
                name,
                new MaterialArrayParser(),
                "",
                TypeToken.get(Material[].class),
                suggestionsProvider,
                defaultDescription
        );
    }

    public static final class MaterialArrayParser implements ArgumentParser<CommandSender, Material[]> {

        @Override
        public ArgumentParseResult<Material[]> parse(CommandContext<CommandSender> context, Queue<String> inputQueue) {
            if (inputQueue.isEmpty()) {
                return ArgumentParseResult.failure(new NoInputProvidedException(MaterialArrayParser.class, context));
            }

            try {
                Material[] materials = new Material[inputQueue.size()];
                for (int i = 0; i < materials.length; i++) {
                    materials[i] = Material.valueOf(inputQueue.peek().toUpperCase());
                    inputQueue.remove();
                }
                return ArgumentParseResult.success(materials);
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
            return StringUtils.findThatStartsWith(SUGGESTION_BLOCKS, input);
        }
    }
}
