package dev.frankheijden.insights.api.reflection;

import dev.frankheijden.minecraftreflection.ClassObject;
import dev.frankheijden.minecraftreflection.MinecraftReflection;
import org.bukkit.Material;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public class RTileEntityTypes {

    private static final MinecraftReflection reflection = MinecraftReflection
            .of("net.minecraft.server.%s.TileEntityTypes");
    private static final Set<Material> TILE_ENTITY_MATERIALS;

    static {
        Set<Material> materials = new HashSet<>();
        for (Field field : reflection.getClazz().getFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || !field.getType().equals(reflection.getClazz())) continue;

            Object tileEntityTypes = reflection.get(null, field.getName());
            Set<?> set = reflection.get(tileEntityTypes, "J");
            for (Object block : set) {
                Object item = RBlock.getReflection().invoke(block, "getItem");
                ClassObject<?> param = ClassObject.of(RItem.getReflection().getClazz(), item);
                materials.add(RCraftMagicNumbers.getReflection().invoke(item, "getMaterial", param));
            }
        }
        materials.removeIf(Material::isAir);
        TILE_ENTITY_MATERIALS = Collections.unmodifiableSet(EnumSet.copyOf(materials));
    }

    private RTileEntityTypes() {}

    public static boolean isTileEntity(Material m) {
        return TILE_ENTITY_MATERIALS.contains(m);
    }

    public static Set<Material> getTileEntityMaterials() {
        return TILE_ENTITY_MATERIALS;
    }
}
