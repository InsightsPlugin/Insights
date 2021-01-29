package dev.frankheijden.insights.managers;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FreezeManager {

    private static FreezeManager instance;

    private final Set<UUID> frozen;

    public FreezeManager() {
        instance = this;

        this.frozen = new HashSet<>();
    }

    public static FreezeManager getInstance() {
        return instance;
    }

    public void freezePlayer(UUID uuid) {
        frozen.add(uuid);
    }

    public boolean isFrozen(UUID uuid) {
        return frozen.contains(uuid);
    }

    public void defrostPlayer(UUID uuid) {
        frozen.remove(uuid);
    }
}
