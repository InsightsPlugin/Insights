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

    public DistributionStorage(Map<ScanObject<?>, Integer> map) {
        super(map);
    }

    /**
     * Constructs a new DistributionStorage from given material and entity distributions.
     */
    public static DistributionStorage of(Distribution<Material> materials, Distribution<EntityType> entities) {
        Map<ScanObject<?>, Integer> map = new ConcurrentHashMap<>();
        for (Map.Entry<Material, Integer> entry : materials.distributionMap.entrySet()) {
            map.put(ScanObject.of(entry.getKey()), entry.getValue());
        }
        for (Map.Entry<EntityType, Integer> entry : entities.distributionMap.entrySet()) {
            map.put(ScanObject.of(entry.getKey()), entry.getValue());
        }
        return new DistributionStorage(map);
    }

    @Override
    public DistributionStorage copy(Map<ScanObject<?>, Integer> map) {
        map.putAll(distributionMap);
        return new DistributionStorage(map);
    }
}
