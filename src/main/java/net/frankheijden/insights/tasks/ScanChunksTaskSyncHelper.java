package net.frankheijden.insights.tasks;

import net.frankheijden.insights.Insights;
import net.frankheijden.insights.entities.ScanOptions;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ScanChunksTaskSyncHelper implements Runnable {

    private static final Insights plugin = Insights.getInstance();
    private final ScanOptions scanOptions;
    private final ScanChunksTask scanChunksTask;
    private final Queue<Chunk> chunks;

    private int taskID;
    private int counter;

    public ScanChunksTaskSyncHelper(ScanOptions scanOptions, ScanChunksTask scanChunksTask) {
        this.scanOptions = scanOptions;
        this.scanChunksTask = scanChunksTask;
        this.chunks = new ConcurrentLinkedQueue<>();
    }

    public void start() {
        this.taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 0, 1);
    }

    public void stop() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> Bukkit.getScheduler().cancelTask(taskID), 20);
    }

    public void addChunk(Chunk chunk) {
        this.chunks.add(chunk);
    }

    @Override
    public void run() {
        while (!chunks.isEmpty()) {
            scanChunksTask.addBlockStates(chunks.poll().getTileEntities());
            counter++;
        }

        if (counter == scanOptions.getChunkCount()) {
            stop();
        }
    }
}
