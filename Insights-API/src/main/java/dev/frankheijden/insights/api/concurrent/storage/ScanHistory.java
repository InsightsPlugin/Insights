package dev.frankheijden.insights.api.concurrent.storage;

import dev.frankheijden.insights.api.config.Messages;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ScanHistory {

    private final Map<UUID, Messages.PaginatedMessage<?>> historyMap;

    public ScanHistory() {
        this.historyMap = new ConcurrentHashMap<>();
    }

    public void setHistory(UUID player, Messages.PaginatedMessage<?> message) {
        historyMap.put(player, message);
    }

    public Optional<Messages.PaginatedMessage<?>> getHistory(UUID player) {
        return Optional.ofNullable(historyMap.get(player));
    }
}
