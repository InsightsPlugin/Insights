package dev.frankheijden.insights.commands.util;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.incendo.cloud.SenderMapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommandSenderMapper implements SenderMapper<CommandSourceStack, CommandSender> {
    @Override
    public CommandSender map(CommandSourceStack source) {
        return source.getSender();
    }

    @Override
    public CommandSourceStack reverse(CommandSender sender) {
        return new CommandSourceStack() {
            @Override
            public @NotNull Location getLocation() {
                if (sender instanceof Entity entity) {
                    return entity.getLocation();
                }

                var worlds = Bukkit.getWorlds();
                return new Location(worlds.isEmpty() ? null : worlds.getFirst(), 0, 0, 0); // Best effort lol
            }

            @Override
            public @NotNull CommandSender getSender() {
                return sender;
            }

            @Override
            public @Nullable Entity getExecutor() {
                return sender instanceof Entity entity ? entity : null;
            }
        };
    }
}
