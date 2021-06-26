package dev.frankheijden.insights.api.reflection;

import dev.frankheijden.insights.api.objects.wrappers.ScanObject;
import dev.frankheijden.insights.api.util.SetCollector;
import dev.frankheijden.minecraftreflection.MinecraftReflection;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import org.bukkit.Material;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public class RTileEntityTypes {

    private static final MinecraftReflection reflection = MinecraftReflection.of(TileEntityTypes.class);
    private static final Set<Material> TILE_ENTITY_MATERIALS;
    private static final Set<ScanObject.MaterialObject> TILE_ENTITIES;

    static {
        Set<Material> materials = new HashSet<>();
        try {
            for (Field field : reflection.getClazz().getFields()) {
                if (!Modifier.isStatic(field.getModifiers()) || !field.getType().equals(TileEntityTypes.class))
                    continue;

                TileEntityTypes<?> tileEntityTypes = reflection.get(null, field.getName());
                Set<Block> set = reflection.get(tileEntityTypes, "K");
                for (Block block : set) {
                    materials.add(RCraftMagicNumbers.getMaterial(block));
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
