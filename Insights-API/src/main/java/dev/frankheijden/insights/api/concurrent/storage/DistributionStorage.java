package dev.frankheijden.insights.api.concurrent.storage;

import dev.frankheijden.insights.api.objects.wrappers.ScanObject;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DistributionStorage extends Distribution<ScanObject<?>> implements Storage {

    public DistributionStorage() {
        this(new ConcurrentHashMap<>());
    }

    public DistributionStorage(Map<ScanObject<?>, Long> map) {
        super(map);
    }

    /**
     * Constructs a new DistributionStorage from given material and entity distributions.
     */
    public static DistributionStorage of(Distribution<Material> materials, Distribution<EntityType> entities) {
        return of(materials.distributionMap, entities.distributionMap);
    }

    /**
     * Constructs a new DistributionStorage from given material and entity distribution maps.
     */
    public static DistributionStorage of(Map<Material, Long> materials, Map<EntityType, Long> entities) {
        Map<ScanObject<?>, Long> map = new ConcurrentHashMap<>();
        for (Map.Entry<Material, Long> entry : materials.entrySet()) {
            map.put(ScanObject.of(entry.getKey()), entry.getValue());
        }
        for (Map.Entry<EntityType, Long> entry : entities.entrySet()) {
            map.put(ScanObject.of(entry.getKey()), entry.getValue());
        }
        return new DistributionStorage(map);
    }

    @Override
    public DistributionStorage copy(Map<ScanObject<?>, Long> map) {
        map.putAll(distributionMap);
        return new DistributionStorage(map);
    }
}
