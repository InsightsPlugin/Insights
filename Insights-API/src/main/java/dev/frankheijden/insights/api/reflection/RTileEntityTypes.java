package dev.frankheijden.insights.api.reflection;

import dev.frankheijden.insights.api.objects.wrappers.ScanObject;
import dev.frankheijden.insights.api.util.SetCollector;
import dev.frankheijden.minecraftreflection.ClassObject;
import dev.frankheijden.minecraftreflection.MinecraftReflection;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RTileEntityTypes {
    private static final Set<Material> TILE_ENTITY_MATERIALS;
    private static final Set<ScanObject.MaterialObject> TILE_ENTITIES;

    static {
        var blockEntityTypeReflection = MinecraftReflection.of(
                "net.minecraft.world.level.block.entity.BlockEntityType" // TODO: fix
        );
        var craftBlockDataReflection = MinecraftReflection.of(
                "org.bukkit.craftbukkit.%s.block.data.CraftBlockData"
        );
        var blockStateReflection = MinecraftReflection.of(
                "net.minecraft.world.level.block.state.BlockState" // TODO: fix
        );
        Map<Material, Object> blockStateMap = new EnumMap<>(Material.class);
        for (Material m : Material.values()) {
            if (m.isBlock()) {
                blockStateMap.put(m, craftBlockDataReflection.invoke(Bukkit.createBlockData(m), "getState"));
            }
        }

        Set<Material> materials = new HashSet<>();
        try {
            for (Field field : blockEntityTypeReflection.getClazz().getFields()) {
                if (!Modifier.isStatic(field.getModifiers())) continue;
                if (!field.getType().equals(blockEntityTypeReflection.getClazz())) continue;

                Object tileEntityTypes = blockEntityTypeReflection.get(null, field.getName());
                for (Map.Entry<Material, Object> entry : blockStateMap.entrySet()) {
                    var obj = ClassObject.of(blockStateReflection.getClazz(), entry.getValue());
                    if (blockEntityTypeReflection.invoke(tileEntityTypes, "isValid", obj)) {
                        materials.add(entry.getKey());
                    }
                }
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }

        materials.removeIf(Material::isAir);
        TILE_ENTITY_MATERIALS = Collections.unmodifiableSet(EnumSet.copyOf(materials));
        TILE_ENTITIES = TILE_ENTITY_MATERIALS.stream()
                .map(ScanObject::of)
                .collect(SetCollector.unmodifiableSet());
    }

    private RTileEntityTypes() {}

    public static boolean isTileEntity(Material m) {
        return TILE_ENTITY_MATERIALS.contains(m);
    }

    public static Set<Material> getTileEntityMaterials() {
        return TILE_ENTITY_MATERIALS;
    }

    public static Set<ScanObject.MaterialObject> getTileEntities() {
        return TILE_ENTITIES;
    }
}
