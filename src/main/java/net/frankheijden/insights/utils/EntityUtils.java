package net.frankheijden.insights.utils;

import net.frankheijden.insights.managers.NMSManager;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SpawnEggMeta;

import java.util.EnumMap;
import java.util.Map;

public class EntityUtils {

    private static final Map<EntityType, Material> NON_SPAWN_EGG_ENTITIES = new EnumMap<>(EntityType.class);
    static {
        NON_SPAWN_EGG_ENTITIES.put(EntityType.MINECART, Material.MINECART);
        NON_SPAWN_EGG_ENTITIES.put(EntityType.MINECART_HOPPER, Material.HOPPER_MINECART);
        if (NMSManager.getInstance().isPost(13)) {
            NON_SPAWN_EGG_ENTITIES.put(EntityType.MINECART_CHEST, Material.CHEST_MINECART);
            NON_SPAWN_EGG_ENTITIES.put(EntityType.MINECART_COMMAND, Material.COMMAND_BLOCK_MINECART);
            NON_SPAWN_EGG_ENTITIES.put(EntityType.MINECART_FURNACE, Material.FURNACE_MINECART);
            NON_SPAWN_EGG_ENTITIES.put(EntityType.MINECART_TNT, Material.TNT_MINECART);
        } else {
            NON_SPAWN_EGG_ENTITIES.put(EntityType.MINECART_CHEST, Material.valueOf("STORAGE_MINECART"));
            NON_SPAWN_EGG_ENTITIES.put(EntityType.MINECART_COMMAND, Material.valueOf("COMMAND_MINECART"));
            NON_SPAWN_EGG_ENTITIES.put(EntityType.MINECART_FURNACE, Material.valueOf("POWERED_MINECART"));
            NON_SPAWN_EGG_ENTITIES.put(EntityType.MINECART_TNT, Material.valueOf("EXPLOSIVE_MINECART"));
        }

        NON_SPAWN_EGG_ENTITIES.put(EntityType.ITEM_FRAME, Material.ITEM_FRAME);
        NON_SPAWN_EGG_ENTITIES.put(EntityType.PAINTING, Material.PAINTING);
        NON_SPAWN_EGG_ENTITIES.put(EntityType.ARMOR_STAND, Material.ARMOR_STAND);
    }

    private static String getWoodName(TreeSpecies species) {
        switch (species) {
            case GENERIC: return "OAK";
            case REDWOOD: return "SPRUCE";
            default: return species.name();
        }
    }

    private static Material getMaterial(Entity entity) {
        Material m = NON_SPAWN_EGG_ENTITIES.get(entity.getType());
        if (m != null) return m;

        if (entity instanceof Boat) {
            if (NMSManager.getInstance().isPost(12)) {
                Boat boat = (Boat) entity;
                String wood = getWoodName(boat.getWoodType());
                if (NMSManager.getInstance().isPost(13)) {
                    return Material.valueOf(wood + "_BOAT");
                } else if (wood.equals("OAK")) {
                    return Material.valueOf("BOAT");
                } else {
                    return Material.valueOf("BOAT_" + wood);
                }
            } else {
                return Material.valueOf("BOAT");
            }
        }

        if (NMSManager.getInstance().isPost(13)) {
            return Material.valueOf(entity.getType().name() + "_SPAWN_EGG");
        } else {
            return Material.valueOf("MONSTER_EGG");
        }
    }

    public static ItemStack createItemStack(Entity entity, int count) {
        ItemStack is = new ItemStack(getMaterial(entity), count);
        if (!NMSManager.getInstance().isPost(13) && is.getType().name().equals("MONSTER_EGG")) {
            SpawnEggMeta eggMeta = (SpawnEggMeta) is.getItemMeta();
            eggMeta.setSpawnedType(entity.getType());
            is.setItemMeta(eggMeta);
        }
        return is;
    }
}
