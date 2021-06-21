package dev.frankheijden.insights.api.reflection;

import dev.frankheijden.minecraftreflection.MinecraftReflection;
import dev.frankheijden.minecraftreflection.Reflection;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import org.bukkit.World;

public class RWorldServer {

    private static final MinecraftReflection reflection = MinecraftReflection
            .of("net.minecraft.server.level.WorldServer");

    private static MethodHandle entityManagerMethodHandle;

    static {
        try {
            entityManagerMethodHandle = MethodHandles.lookup().unreflectGetter(Reflection.getAccessibleField(
                    reflection.getClazz(),
                    "G"
            ));
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    private RWorldServer() {}

    @SuppressWarnings("unchecked")
    public static PersistentEntitySectionManager<Entity> getPersistentEntityManager(World world) throws Throwable {
        Object serverLevel = RCraftWorld.getServerLevel(world);
        return (PersistentEntitySectionManager<Entity>) entityManagerMethodHandle.invoke(serverLevel);
    }
}
