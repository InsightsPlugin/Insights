package dev.frankheijden.insights.commands.parsers;

import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.NonNull;

public class WorldParser implements ArgumentParser<CommandSender, World> {

    @Override
    public @NonNull ArgumentParseResult<World> parse(
            @NonNull CommandContext<CommandSender> cxt,
            @NonNull Queue<String> inputQueue
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
    public @NonNull List<String> suggestions(@NonNull CommandContext<CommandSender> context, @NonNull String input) {
        List<World> worlds = Bukkit.getWorlds();
        List<String> worldNames = new ArrayList<>(worlds.size());
        for (World world : worlds) {
            worldNames.add(world.getName());
        }
        return worldNames;
    }
}
