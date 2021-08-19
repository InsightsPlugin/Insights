package dev.frankheijden.insights.api.reflection;

import dev.frankheijden.insights.api.objects.wrappers.ScanObject;
import dev.frankheijden.insights.api.util.SetCollector;
import dev.frankheijden.minecraftreflection.MinecraftReflection;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_17_R1.block.data.CraftBlockData;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RTileEntityTypes {

    private static final MinecraftReflection reflection = MinecraftReflection.of(BlockEntityType.class);
    private static final Set<Material> TILE_ENTITY_MATERIALS;
    private static final Set<ScanObject.MaterialObject> TILE_ENTITIES;

    static {
        Map<Material, BlockState> blockStateMap = new EnumMap<>(Material.class);
        for (Material m : Material.values()) {
            if (m.isBlock()) {
                blockStateMap.put(m, ((CraftBlockData) Bukkit.createBlockData(m)).getState());
            }
        }

        Set<Material> materials = new HashSet<>();
        try {
            for (Field field : reflection.getClazz().getFields()) {
                if (!Modifier.isStatic(field.getModifiers()) || !field.getType().equals(BlockEntityType.class))
                    continue;

                BlockEntityType<?> tileEntityTypes = reflection.get(null, field.getName());
                for (Map.Entry<Material, BlockState> entry : blockStateMap.entrySet()) {
                    if (tileEntityTypes.isValid(entry.getValue())) {
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
