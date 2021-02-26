package dev.frankheijden.insights.api.utils;

import dev.frankheijden.insights.api.util.SetCollector;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MaterialUtils {

    private MaterialUtils() {}

    public static final Set<Material> BLOCKS = Arrays.stream(Material.values())
            .filter(Material::isBlock)
            .collect((SetCollector<Material>) HashSet::new);
    public static final Set<EntityType> ENTITIES = Arrays.stream(EntityType.values())
            .collect((SetCollector<EntityType>) HashSet::new);

}
