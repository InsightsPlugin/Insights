package net.frankheijden.insights.tasks;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;

import java.util.ArrayList;
import java.util.List;

public class ScanChunksTaskSyncHelper implements Runnable {
    private ScanChunksTask scanChunksTask;
    private transient List<Chunk> chunks;
    private transient List<Chunk> chunksToAdd;

    private int taskID;
    private boolean run;
    private boolean cancelled;

    public ScanChunksTaskSyncHelper(ScanChunksTask scanChunksTask) {
        this.scanChunksTask = scanChunksTask;
        this.chunks = new ArrayList<>();
        this.chunksToAdd = new ArrayList<>();
    }

    public void start() {
        this.taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(scanChunksTask.getLoadChunksTask().getPlugin(), this, 0, 1);
        this.run = true;
    }

    public void stop() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(scanChunksTask.getLoadChunksTask().getPlugin(), new Runnable() {
            @Override
            public void run() {
                Bukkit.getScheduler().cancelTask(taskID);
                cancelled = true;
            }
        }, 20);
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void addChunk(Chunk chunk) {
        this.chunksToAdd.add(chunk);
    }

    @Override
    public void run() {
        if (!this.run) {
            return;
        }
        this.run = false;

        List<Chunk> chunksToRemove = new ArrayList<>();
        for (Chunk chunk : chunks) {
            scanChunksTask.addBlockStates(chunk.getTileEntities());
            chunksToRemove.add(chunk);
        }

        chunks.removeAll(chunksToRemove);
        chunks.addAll(chunksToAdd);
        chunksToAdd.clear();

        this.run = true;
    }
}
