package net.frankheijden.insights.managers;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.bukkit.*;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.eventbus.EventHandler;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import net.frankheijden.insights.entities.*;
import net.frankheijden.insights.listeners.WorldEditListener;
import org.bukkit.*;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class WorldEditManager {

    private static WorldEditManager instance;

    private final WorldEditPlugin wePlugin;

    public WorldEditManager() {
        instance = this;

        this.wePlugin = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");

        register();
    }

    private void register() {
        this.wePlugin.getWorldEdit().getEventBus().register(this);
    }

    public static WorldEditManager getInstance() {
        return instance;
    }

    @Subscribe(priority = EventHandler.Priority.VERY_LATE)
    public void handleEditSession(EditSessionEvent event) {
        Actor actor = event.getActor();
        if (actor != null && actor.isPlayer()) {
            Player player = Bukkit.getPlayer(actor.getName());
            event.setExtent(WorldEditListener.from(player, event.getExtent()));
        }
    }

    public Selection getSelection(Player player) {
        LocalSession session = wePlugin.getSession(player);
        if (session == null) return null;
        Region region;
        try {
            region = session.getSelection(session.getSelectionWorld());
        } catch (IncompleteRegionException ex) {
            return null;
        }
        return adapt(region);
    }

    public static Selection adapt(Region region) {
        if (region.getWorld() == null) return null;
        World world = Bukkit.getWorld(region.getWorld().getName());

        return new Selection(
                adapt(world, getVector(region, "getMinimumPoint")),
                adapt(world, getVector(region, "getMaximumPoint"))
        );
    }

    public static Object getVector(Object region, String method) {
        try {
            Class<?> regionClass = Region.class;
            Method m = regionClass.getDeclaredMethod(method);
            return m.invoke(region);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Location adapt(World world, Object vector) {
        try {
            Class<?> vectorClass = Class.forName("com.sk89q.worldedit.Vector");
            return adapt(world, vector, vectorClass);
        } catch (ClassNotFoundException ex) {
            return adapt7(world, vector);
        }
    }

    public static Location adapt7(World world, Object vector) {
        try {
            Class<?> blockVectorClass = Class.forName("com.sk89q.worldedit.math.BlockVector3");
            return adapt(world, vector, blockVectorClass);
        } catch (ClassNotFoundException ignored) {

        }
        return null;
    }

    public static Location adapt(World world, Object vector, Class<?> vectorClass) {
        try {
            Method getX = vectorClass.getDeclaredMethod("getX");
            Method getY = vectorClass.getDeclaredMethod("getY");
            Method getZ = vectorClass.getDeclaredMethod("getZ");
            // Weird way to do it, but WE 7 returns an Integer here.
            // So convert to string, then parse as double.
            double x = Double.parseDouble(getX.invoke(vector).toString());
            double y = Double.parseDouble(getY.invoke(vector).toString());
            double z = Double.parseDouble(getZ.invoke(vector).toString());
            return new Location(world, x, y, z);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
