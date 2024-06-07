package dev.frankheijden.insights.listeners.manager;

import dev.frankheijden.insights.Insights;
import dev.frankheijden.insights.api.annotations.AllowDisabling;
import dev.frankheijden.insights.api.annotations.AllowPriorityOverride;
import dev.frankheijden.insights.api.listeners.InsightsListener;
import dev.frankheijden.insights.api.listeners.manager.InsightsListenerManager;
import dev.frankheijden.insights.listeners.BlockListener;
import dev.frankheijden.insights.listeners.ChunkListener;
import dev.frankheijden.insights.listeners.EntityListener;
import dev.frankheijden.insights.listeners.PaperBlockListener;
import dev.frankheijden.insights.listeners.PaperEntityListener;
import dev.frankheijden.insights.listeners.PistonListener;
import dev.frankheijden.insights.listeners.PlayerListener;
import dev.frankheijden.insights.listeners.WorldListener;
import dev.frankheijden.insights.nms.core.ReflectionUtils;
import io.papermc.lib.PaperLib;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.RegisteredListener;

public class ListenerManager implements InsightsListenerManager {

    private static final Map<String, Method> ALLOWED_DISABLE_EVENTS;
    private static final Map<String, Method> ALLOWED_PRIORITY_OVERRIDE_EVENTS;

    static {
        List<Method> disableMethods = new ArrayList<>();
        disableMethods.addAll(ReflectionUtils.getAnnotatedMethods(BlockListener.class, AllowDisabling.class));
        disableMethods.addAll(ReflectionUtils.getAnnotatedMethods(WorldListener.class, AllowDisabling.class));
        if (PaperLib.isPaper()) {
            disableMethods.addAll(ReflectionUtils.getAnnotatedMethods(PaperBlockListener.class, AllowDisabling.class));
        }
        ALLOWED_DISABLE_EVENTS = getEventClassMap(disableMethods);

        List<Method> priorityMethods = new ArrayList<>();
        priorityMethods.addAll(ReflectionUtils.getAnnotatedMethods(BlockListener.class, AllowPriorityOverride.class));
        priorityMethods.addAll(ReflectionUtils.getAnnotatedMethods(EntityListener.class, AllowPriorityOverride.class));
        priorityMethods.addAll(ReflectionUtils.getAnnotatedMethods(PistonListener.class, AllowPriorityOverride.class));
        ALLOWED_PRIORITY_OVERRIDE_EVENTS = getEventClassMap(priorityMethods);
    }

    private static Map<String, Method> getEventClassMap(List<Method> listenerMethods) {
        Map<String, Method> map = new HashMap<>();

        for (Method method : listenerMethods) {
            Class<?>[] params = method.getParameterTypes();
            if (params.length != 1 || !Event.class.isAssignableFrom(params[0])) continue;

            map.put(params[0].getSimpleName().toUpperCase(Locale.ENGLISH), method);
        }

        return Collections.unmodifiableMap(map);
    }

    private final Insights plugin;
    private final PlayerListener playerListener;
    private final ChunkListener chunkListener;
    private final BlockListener blockListener;
    private final WorldListener worldListener;
    private final PaperEntityListener paperEntityListener;
    private final PaperBlockListener paperBlockListener;
    private final EntityListener entityListener;
    private final PistonListener pistonListener;

    /**
     * Constructs the ListenerManager with all Insights listeners.
     */
    public ListenerManager(Insights plugin) {
        this.plugin = plugin;
        this.playerListener = new PlayerListener(plugin);
        this.chunkListener = new ChunkListener(plugin);
        this.blockListener = new BlockListener(plugin);
        this.worldListener = new WorldListener(plugin);
        this.paperEntityListener = PaperLib.isPaper() ? new PaperEntityListener(plugin) : null;
        this.paperBlockListener = PaperLib.isPaper() ? new PaperBlockListener(plugin) : null;
        this.entityListener = PaperLib.isPaper() ? null : new EntityListener(plugin);
        this.pistonListener = new PistonListener(plugin);
    }

    @Override
    public void register() {
        List<InsightsListener> listeners = new ArrayList<>();
        listeners.add(playerListener);
        listeners.add(chunkListener);

        List<InsightsListener> disableListeners = new ArrayList<>();
        disableListeners.add(blockListener);
        disableListeners.add(worldListener);

        if (!plugin.getSettings().REDSTONE_UPDATE_LIMITER_ENABLED) {
            BlockRedstoneEvent.getHandlerList().unregister(blockListener);
        }

        if (PaperLib.isPaper()) {
            listeners.add(paperEntityListener);
            disableListeners.add(paperBlockListener);
        } else {
            listeners.add(entityListener);
        }

        if (plugin.getSettings().APPLY_PISTON_LIMITS) {
            listeners.add(pistonListener);
        }

        listeners.addAll(disableListeners);
        listeners.forEach(listener -> plugin.getServer().getPluginManager().registerEvents(listener, plugin));

        Function<Class<?>, HandlerList> getHandlerList = (Class<?> clazz) -> {
            try {
                return (HandlerList) clazz.getMethod("getHandlerList").invoke(null);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
                throw new RuntimeException(ex);
            }
        };

        for (Class<?> clazz : plugin.getSettings().DISABLED_EVENTS) {
            HandlerList list = getHandlerList.apply(clazz);
            for (InsightsListener listener : disableListeners) {
                list.unregister(listener);
            }
            plugin.getLogger().info("Unregistered listener of '" + clazz.getSimpleName() + "'");
        }

        for (Map.Entry<Class<? extends Event>, EventPriority> e : plugin.getSettings().LISTENER_PRIORITIES.entrySet()) {
            if (e.getValue() == EventPriority.LOWEST) continue;

            HandlerList list = getHandlerList.apply(e.getKey());
            List<RegisteredListener> listenersToUnregister = new ArrayList<>();
            List<RegisteredListener> listenersToRegister = new ArrayList<>();
            for (RegisteredListener listener : list.getRegisteredListeners()) {
                if (plugin.equals(listener.getPlugin()) && listener.getPriority() == EventPriority.LOWEST) {
                    listenersToUnregister.add(listener);

                    String event = e.getKey().getSimpleName();
                    String eventUppercase = event.toUpperCase(Locale.ENGLISH);
                    listenersToRegister.add(new RegisteredListener(
                            listener.getListener(),
                            EventExecutor.create(ALLOWED_PRIORITY_OVERRIDE_EVENTS.get(eventUppercase), e.getKey()),
                            e.getValue(),
                            plugin,
                            listener.isIgnoringCancelled()
                    ));

                    plugin.getLogger().info("Remapped EventPriority of '" + event + "' to '" + e.getValue() + "'");
                }
            }
            listenersToUnregister.forEach(list::unregister);
            listenersToRegister.forEach(list::register);
        }

        if (!plugin.getSettings().REDSTONE_UPDATE_LIMITER_ENABLED) {
            BlockRedstoneEvent.getHandlerList().unregister(blockListener);
        }
    }

    @Override
    public void unregister() {
        HandlerList.unregisterAll(playerListener);
        HandlerList.unregisterAll(chunkListener);
        HandlerList.unregisterAll(blockListener);
        HandlerList.unregisterAll(worldListener);
        if (paperEntityListener != null) HandlerList.unregisterAll(paperEntityListener);
        if (paperBlockListener != null) HandlerList.unregisterAll(paperBlockListener);
        if (entityListener != null) HandlerList.unregisterAll(entityListener);
        HandlerList.unregisterAll(pistonListener);
    }

    @Override
    public Map<String, Method> getAllowedDisableMethods() {
        return ALLOWED_DISABLE_EVENTS;
    }

    @Override
    public Map<String, Method> getAllowedPriorityOverrideMethods() {
        return ALLOWED_PRIORITY_OVERRIDE_EVENTS;
    }

    public PlayerListener getPlayerListener() {
        return playerListener;
    }
}
