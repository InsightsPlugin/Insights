package dev.frankheijden.insights.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.specifier.Range;
import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.commands.InsightsCommand;
import dev.frankheijden.insights.api.config.Messages;
import dev.frankheijden.insights.api.config.notifications.ProgressNotification;
import dev.frankheijden.insights.api.objects.chunk.ChunkLocation;
import dev.frankheijden.insights.api.objects.chunk.ChunkPart;
import dev.frankheijden.insights.api.reflection.RTileEntityTypes;
import dev.frankheijden.insights.api.utils.MaterialUtils;
import dev.frankheijden.insights.api.utils.StringUtils;
import dev.frankheijden.insights.tasks.ScanTask;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommandScan extends InsightsCommand {

    public CommandScan(InsightsPlugin plugin) {
        super(plugin);
    }

    @CommandMethod("scan <radius> tile")
    @CommandPermission("insights.scan.tile")
    private void handleTileScan(
            Player player,
            @Argument("radius") @Range(min = "0", max = "25") int radius
    ) {
        handleScan(player, radius, RTileEntityTypes.getTileEntityMaterials(), false);
    }

    @CommandMethod("scan <radius> all")
    @CommandPermission("insights.scan.all")
    private void handleAllScan(
            Player player,
            @Argument("radius") @Range(min = "0", max = "25") int radius
    ) {
        handleScan(player, radius, null, false);
    }

    @CommandMethod("scan <radius> custom <materials>")
    @CommandPermission("insights.scan.custom")
    private void handleCustomScan(
            Player player,
            @Argument("radius") @Range(min = "0", max = "25") int radius,
            @Argument("materials") Material[] materials
    ) {
        handleScan(player, radius, new HashSet<>(Arrays.asList(materials)), true);
    }

    /**
     * Scans chunks in a radius around a player.
     */
    public void handleScan(Player player, int radius, Set<Material> materials, boolean displayZeros) {
        Chunk chunk = player.getChunk();
        World world = chunk.getWorld();
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();

        // Generate chunk parts
        int edge = (2 * radius) + 1;
        int chunkCount = edge * edge;
        List<ChunkPart> chunkParts = new ArrayList<>(chunkCount);
        for (int x = chunkX - radius; x <= chunkX + radius; x++) {
            for (int z = chunkZ - radius; z <= chunkZ + radius; z++) {
                chunkParts.add(new ChunkLocation(world, x, z).toPart());
            }
        }

        // Create a notification for the task
        ProgressNotification notification = plugin.getNotifications().getCachedProgress(
                player.getUniqueId(),
                Messages.Key.SCAN_PROGRESS
        );
        notification.add(player);

        // Notify about scan start
        plugin.getMessages().getMessage(Messages.Key.SCAN_START)
                .replace(
                        "count", StringUtils.pretty(chunkCount)
                )
                .color()
                .sendTo(player);

        // Start the scan
        final long start = System.nanoTime();
        ScanTask.scan(plugin, chunkParts, info -> {
            // Update the notification with progress
            double progress = (double) info.getChunksDone() / (double) info.getChunks();
            notification.progress(progress)
                    .create()
                    .replace("percentage", StringUtils.prettyOneDecimal(progress * 100.))
                    .color()
                    .send();
        }, map -> {
            // The time it took to generate the results
            @SuppressWarnings("VariableDeclarationUsageDistance")
            long millis = (System.nanoTime() - start) / 1000000L;

            // Send header
            Messages messages = plugin.getMessages();
            messages.getMessage(Messages.Key.SCAN_FINISH_HEADER).color().sendTo(player);

            // Check which materials we need to display & sort them based on their name.
            List<Material> displayMaterials = new ArrayList<>(materials == null ? map.keySet() : materials);
            displayMaterials.sort(Comparator.comparing(Enum::name));

            // Send each entry
            for (Material material : displayMaterials) {
                // Only display format if nonzero, or displayZeros is set to true.
                int count = map.getOrDefault(material, 0);
                if (count == 0 && !displayZeros) continue;

                messages.getMessage(Messages.Key.SCAN_FINISH_FORMAT)
                        .replace(
                                "entry", MaterialUtils.pretty(material),
                                "count", StringUtils.pretty(count)
                        )
                        .color()
                        .sendTo(player);
            }

            // Send the footer
            messages.getMessage(Messages.Key.SCAN_FINISH_FOOTER)
                    .replace(
                            "chunks", StringUtils.pretty(chunkCount),
                            "blocks", StringUtils.pretty(chunkCount * 256 * 16 * 16),
                            "time", StringUtils.pretty(Duration.ofMillis(millis))
                    )
                    .color()
                    .sendTo(player);
        });
    }
}
