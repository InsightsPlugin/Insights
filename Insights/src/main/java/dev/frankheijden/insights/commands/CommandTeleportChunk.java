package dev.frankheijden.insights.commands;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.commands.InsightsCommand;
import dev.frankheijden.insights.api.config.Messages;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Flag;
import org.incendo.cloud.annotations.Permission;

public class CommandTeleportChunk extends InsightsCommand {

    public CommandTeleportChunk(InsightsPlugin plugin) {
        super(plugin);
    }

    @Command("teleportchunk|tpchunk <world> <x> <z>")
    @Permission("insights.teleportchunk")
    private void handleTeleportChunk(
            Player player,
            @Argument("world") World world,
            @Argument("x") int chunkX,
            @Argument("z") int chunkZ,
            @Flag(value = "generate", aliases = { "g" }) boolean generate
    ) {
        var chunkTp = plugin.getChunkTeleport();
        var messages = plugin.getMessages();
        chunkTp.teleportPlayerToChunk(player, world, chunkX, chunkZ, generate).whenComplete((res, err) -> {
            Messages.Message message;
            if (err != null) {
                message = messages.getMessage(Messages.Key.TELEPORTCHUNK_ERROR);
            } else {
                message = switch (res) {
                    case NOT_GENERATED -> messages.getMessage(Messages.Key.TELEPORTCHUNK_NOT_GENERATED);
                    case FAILED -> messages.getMessage(Messages.Key.TELEPORTCHUNK_FAILED);
                    case SUCCESS -> messages.getMessage(Messages.Key.TELEPORTCHUNK_SUCCESS).addTemplates(
                            Messages.tagOf("world", world.getName()),
                            Messages.tagOf("chunk-x", chunkX),
                            Messages.tagOf("chunk-z", chunkZ)
                    );
                    default -> throw new IllegalArgumentException("Unhandled result case: " + res);
                };
            }
            message.sendTo(player);
        });
    }
}
