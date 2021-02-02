package dev.frankheijden.insights.api.concurrent;

import org.bukkit.entity.Player;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerList implements Iterable<Map.Entry<UUID, Player>> {

    private final Map<UUID, Player> players;

    /**
     * Constructs a new Thread-safe PlayerList with given online players.
     */
    public PlayerList(Collection<? extends Player> players) {
        this.players = new ConcurrentHashMap<>();
        for (Player player : players) {
            addPlayer(player);
        }
    }

    public void addPlayer(Player player) {
        this.players.put(player.getUniqueId(), player);
    }

    public void removePlayer(Player player) {
        this.players.remove(player.getUniqueId());
    }

    public int size() {
        return this.players.size();
    }

    @Override
    public Iterator<Map.Entry<UUID, Player>> iterator() {
        return players.entrySet().iterator();
    }
}
