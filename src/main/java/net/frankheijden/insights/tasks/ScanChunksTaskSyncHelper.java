package net.frankheijden.insights.tasks;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class ScanChunksTaskSyncHelper implements Runnable {
    private ScanChunksTask scanChunksTask;
    private final Vector<Chunk> chunks;

    private int taskID;
    private int counter;

    public ScanChunksTaskSyncHelper(ScanChunksTask scanChunksTask) {
        this.scanChunksTask = scanChunksTask;
        this.chunks = new Vector<>();
    }

    public void start() {
        this.taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(scanChunksTask.getLoadChunksTask().getPlugin(), this, 0, 1);
    }

    public void stop() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(scanChunksTask.getLoadChunksTask().getPlugin(), () -> Bukkit.getScheduler().cancelTask(taskID), 20);
    }

    public void addChunk(Chunk chunk) {
        this.chunks.add(chunk);
    }

    @Override
    public void run() {
        synchronized (chunks) {
            List<Chunk> chunksToRemove = new ArrayList<>();
            for (Chunk chunk : chunks) {
                scanChunksTask.addBlockStates(chunk.getTileEntities());
                chunksToRemove.add(chunk);
                counter++;
            }
            chunks.removeAll(chunksToRemove);
        }

        if (counter == scanChunksTask.getLoadChunksTask().getTotalChunks()) {
            stop();
        }
    }
}
