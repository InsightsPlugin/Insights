package dev.frankheijden.insights.api.reflection;

import dev.frankheijden.insights.api.objects.wrappers.ScanObject;
import dev.frankheijden.insights.api.util.SetCollector;
import dev.frankheijden.insights.nms.core.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
        try {
            var tileEntityTypesClazz = Class.forName(
                    "net.minecraft.world.level.block.entity.TileEntityTypes"
            );
            var craftBlockDataClazz = Class.forName(
                    "org.bukkit.craftbukkit.block.data.CraftBlockData"
            );
            var blockDataClazz = Class.forName(
                    "net.minecraft.world.level.block.state.IBlockData"
            );
            MethodHandle isValidMethodHandle;
            try {
                isValidMethodHandle = MethodHandles.lookup().unreflect(ReflectionUtils.findDeclaredMethod(
                        tileEntityTypesClazz,
                        new Class[]{ blockDataClazz },
                        boolean.class,
                        "isValid"
                ));
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }

            Map<Material, Object> blockStateMap = new EnumMap<>(Material.class);
            for (Material m : Material.values()) {
                if (m.isBlock()) {
                    blockStateMap.put(m, craftBlockDataClazz.getMethod("getState").invoke(Bukkit.createBlockData(m)));
                }
            }

            Set<Material> materials = new HashSet<>();
            try {
                for (Field field : tileEntityTypesClazz.getFields()) {
                    if (!Modifier.isStatic(field.getModifiers())) continue;
                    if (!field.getType().equals(tileEntityTypesClazz)) continue;

                    Object tileEntityTypes = field.get(null);
                    for (Map.Entry<Material, Object> entry : blockStateMap.entrySet()) {
                        if ((boolean) isValidMethodHandle.invoke(tileEntityTypes, entry.getValue())) {
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
        } catch (ClassNotFoundException
                 | NoSuchMethodException
                 | InvocationTargetException
                 | IllegalAccessException ex
        ) {
            throw new RuntimeException(ex);
        }
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
