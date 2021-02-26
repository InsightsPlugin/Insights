package dev.frankheijden.insights.api.concurrent.storage;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class AddonStorage {

    private final Map<String, Storage> distributionMap;

    public AddonStorage() {
        this.distributionMap = new ConcurrentHashMap<>();
    }

    public Optional<Storage> get(String key) {
        return Optional.ofNullable(distributionMap.get(key));
    }

    public void put(String key, Storage storage) {
        this.distributionMap.put(key, storage);
    }

    public void remove(String key) {
        this.distributionMap.remove(key);
    }
}
