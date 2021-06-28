package dev.frankheijden.insights.api.reflection;

import dev.frankheijden.minecraftreflection.MinecraftReflection;
import dev.frankheijden.minecraftreflection.Reflection;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityPersistentStorage;
import net.minecraft.world.level.entity.EntitySectionStorage;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;

public class RPersistentEntitySectionManager {

    private static final MinecraftReflection reflection = MinecraftReflection.of(PersistentEntitySectionManager.class);

    private static MethodHandle sectionStorageMethodHandle;
    private static MethodHandle permanentStorageMethodHandle;

    static {
        try {
            sectionStorageMethodHandle = MethodHandles.lookup().unreflectGetter(Reflection.getAccessibleField(
                    reflection.getClazz(),
                    "f"
            ));
            permanentStorageMethodHandle = MethodHandles.lookup().unreflectGetter(Reflection.getAccessibleField(
                    reflection.getClazz(),
                    "d"
            ));
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    private RPersistentEntitySectionManager() {}

    @SuppressWarnings("unchecked")
    public static EntitySectionStorage<Entity> getSectionStorage(
            Object persistentEntitySectionManager
    ) throws Throwable {
        return (EntitySectionStorage<Entity>) sectionStorageMethodHandle.invoke(persistentEntitySectionManager);
    }

    @SuppressWarnings("unchecked")
    public static EntityPersistentStorage<Entity> getPermanentStorage(
            Object persistentEntitySectionManager
    ) throws Throwable {
        return (EntityPersistentStorage<Entity>) permanentStorageMethodHandle.invoke(persistentEntitySectionManager);
    }
}
