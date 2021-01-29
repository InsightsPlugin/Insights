package dev.frankheijden.insights.managers;

import dev.frankheijden.insights.tasks.LoadChunksTask;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ScanManager {

    private static ScanManager instance;
    private final Map<UUID, LoadChunksTask> dataMap;

    public ScanManager() {
        instance = this;
        dataMap = new ConcurrentHashMap<>();
    }

    public static ScanManager getInstance() {
        return instance;
    }

    public LoadChunksTask getTask(Player player) {
        return getTask(player.getUniqueId());
    }

    public LoadChunksTask getTask(UUID uuid) {
        return dataMap.get(uuid);
    }

    public void putTask(Player player, LoadChunksTask loadChunksTask) {
        dataMap.put(player.getUniqueId(), loadChunksTask);
    }

    public void putTask(UUID uuid, LoadChunksTask loadChunksTask) {
        dataMap.put(uuid, loadChunksTask);
    }

    public boolean isScanning(Player player) {
        return isScanning(player.getUniqueId());
    }

    public boolean isScanning(UUID uuid) {
        return dataMap.containsKey(uuid);
    }

    public void remove(Player player) {
        remove(player.getUniqueId());
    }

    public void remove(UUID uuid) {
        dataMap.remove(uuid);
    }
}
