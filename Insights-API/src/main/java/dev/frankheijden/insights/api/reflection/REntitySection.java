package dev.frankheijden.insights.api.reflection;

import dev.frankheijden.minecraftreflection.MinecraftReflection;
import dev.frankheijden.minecraftreflection.Reflection;
import net.minecraft.util.EntitySlice;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntitySection;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.function.Consumer;

public class REntitySection {

    private static final MinecraftReflection reflection = MinecraftReflection.of(EntitySection.class);

    private static MethodHandle storageHandle;

    static {
        try {
            storageHandle = MethodHandles.lookup().unreflectGetter(Reflection.getAccessibleField(
                    reflection.getClazz(),
                    "b"
            ));
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    private REntitySection() {}

    /**
     * Iterates over the entities in an EntitySection.
     */
    @SuppressWarnings("unchecked")
    public static void iterate(EntitySection<Entity> entitySection, Consumer<Entity> entityConsumer) throws Throwable {
        EntitySlice<Entity> storage = (EntitySlice<Entity>) storageHandle.invoke(entitySection);
        REntitySlice.iterate(storage, entityConsumer);
    }
}
