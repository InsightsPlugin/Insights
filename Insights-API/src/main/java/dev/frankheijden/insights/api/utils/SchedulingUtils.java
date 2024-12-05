package dev.frankheijden.insights.api.utils;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class SchedulingUtils {
    public static final boolean IS_FOLIA = ClassUtils.isClassLoaded(
            "io.papermc.paper.threadedregions.RegionizedServer"
    );
    public static final long TICK_TO_MILLISECONDS = 50L;
    private static final long FOLIA_MINIMUM_DELAY = 1L;

    public static void runImmediatelyAtEntityIfFolia(Plugin plugin, Entity entity, Runnable task) {
        if (IS_FOLIA) {
            entity.getScheduler().execute(plugin, task, null, FOLIA_MINIMUM_DELAY);
        } else {
            task.run();
        }
    }

    public static void runImmediatelyAtChunkIfFolia(Plugin plugin, World world, int chunkX, int chunkZ, Runnable task) {
        if (IS_FOLIA) {
            plugin.getServer().getRegionScheduler().execute(plugin, world, chunkX, chunkZ, task);
        } else {
            task.run();
        }
    }

    public static void runImmediatelyAtChunk(Plugin plugin, World world, int chunkX, int chunkZ, Runnable task) {
        plugin.getServer().getRegionScheduler().execute(plugin, world, chunkX, chunkZ, task);
    }

    public static void runImmediatelyAtLocationIfFolia(Plugin plugin, Location location, Runnable task) {
        if (IS_FOLIA) {
            plugin.getServer().getRegionScheduler().execute(plugin, location, task);
        } else {
            task.run();
        }
    }

    public static ScheduledTask runTaskTimerAsynchronously(
            Plugin plugin, Runnable runnable, long delayTicks, long periodTicks
    ) {
        return plugin.getServer().getAsyncScheduler().runAtFixedRate(
                plugin,
                wrapRunnable(runnable),
                delayTicks * TICK_TO_MILLISECONDS,
                periodTicks * TICK_TO_MILLISECONDS,
                TimeUnit.MILLISECONDS
        );
    }

    public static Consumer<ScheduledTask> wrapRunnable(Runnable runnable) {
        return task -> runnable.run();
    }
}
