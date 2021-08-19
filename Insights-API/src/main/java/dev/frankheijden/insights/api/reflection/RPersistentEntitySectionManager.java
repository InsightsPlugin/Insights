package dev.frankheijden.insights.api.reflection;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityPersistentStorage;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;

public class RPersistentEntitySectionManager {

    private static MethodHandle permanentStorageMethodHandle;

    static {
        try {
            Field permanentStorageField = null;
            for (Field field : PersistentEntitySectionManager.class.getDeclaredFields()) {
                if (field.getType().equals(EntityPersistentStorage.class)) {
                    permanentStorageField = field;
                    break;
                }
            }

            if (permanentStorageField == null) {
                throw new IllegalStateException("Could not find field PersistentEntitySectionManager#permanentStorage");
            }

            permanentStorageField.setAccessible(true);
            permanentStorageMethodHandle = MethodHandles.lookup().unreflectGetter(permanentStorageField);
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    private RPersistentEntitySectionManager() {}

    @SuppressWarnings("unchecked")
    public static EntityPersistentStorage<Entity> getPermanentStorage(
            PersistentEntitySectionManager<Entity> entityManager
    ) throws Throwable {
        return (EntityPersistentStorage<Entity>) permanentStorageMethodHandle.invoke(entityManager);
    }
}
