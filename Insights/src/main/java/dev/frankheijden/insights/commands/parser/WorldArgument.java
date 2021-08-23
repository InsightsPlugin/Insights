package dev.frankheijden.insights.commands.parser;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import io.leangen.geantyref.TypeToken;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.World;

public class WorldArgument extends CommandArgument<CommandSender, World> {

    /**
     * Constructs a {@link World} argument.
     */
    public WorldArgument(
            boolean required,
            String name,
            BiFunction<CommandContext<CommandSender>, String, List<String>> suggestionsProvider,
            ArgumentDescription defaultDescription
    ) {
        super(
                required,
                name,
                new WorldParser(),
                "",
                new TypeToken<World>() {},
                suggestionsProvider,
                defaultDescription
        );
    }

    public static final class WorldParser implements ArgumentParser<CommandSender, World> {

        @Override
        public ArgumentParseResult<World> parse(
                CommandContext<CommandSender> cxt,
                Queue<String> inputQueue
        ) {
            if (inputQueue.isEmpty()) {
                return ArgumentParseResult.failure(new NoInputProvidedException(
                        WorldParser.class,
                        cxt
                ));
            }

            var world = Bukkit.getWorld(inputQueue.peek());
            if (world == null) {
                return ArgumentParseResult.failure(new IllegalArgumentException(
                        "Invalid World '" + inputQueue.peek() + "'"
                ));
            }

            inputQueue.poll();
            return ArgumentParseResult.success(world);
        }

        @Override
        public boolean isContextFree() {
            return true;
        }

        @Override
        public List<String> suggestions(CommandContext<CommandSender> context, String input) {
            List<World> worlds = Bukkit.getWorlds();
            List<String> worldNames = new ArrayList<>(worlds.size());
            for (World world : worlds) {
                worldNames.add(world.getName());
            }
            return worldNames;
        }
    }
}
