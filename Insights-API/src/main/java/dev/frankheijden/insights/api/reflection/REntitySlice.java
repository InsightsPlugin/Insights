package dev.frankheijden.insights.api.reflection;

import dev.frankheijden.minecraftreflection.MinecraftReflection;
import dev.frankheijden.minecraftreflection.Reflection;
import net.minecraft.util.EntitySlice;
import net.minecraft.world.entity.Entity;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.function.Consumer;

public class REntitySlice {

    private static final MinecraftReflection reflection = MinecraftReflection.of(EntitySlice.class);

    private static MethodHandle allInstancesHandle;

    static {
        try {
            allInstancesHandle = MethodHandles.lookup().unreflectGetter(Reflection.getAccessibleField(
                    reflection.getClazz(),
                    "c"
            ));
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    private REntitySlice() {}

    /**
     * Iterates over the entities in an EntitySlice.
     */
    @SuppressWarnings("unchecked")
    public static void iterate(EntitySlice<Entity> storage, Consumer<Entity> entityConsumer) throws Throwable {
        List<Entity> entities = (List<Entity>) allInstancesHandle.invoke(storage);

        for (var i = 0; i < entities.size(); i++) {
            entityConsumer.accept(entities.get(i));
        }
    }
}
