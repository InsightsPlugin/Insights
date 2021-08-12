package dev.frankheijden.insights.listeners.manager;

import dev.frankheijden.insights.Insights;
import dev.frankheijden.insights.api.annotations.AllowDisabling;
import dev.frankheijden.insights.api.annotations.AllowPriorityOverride;
import dev.frankheijden.insights.api.listeners.InsightsListener;
import dev.frankheijden.insights.api.listeners.manager.InsightsListenerManager;
import dev.frankheijden.insights.api.utils.ReflectionUtils;
import dev.frankheijden.insights.listeners.BlockListener;
import dev.frankheijden.insights.listeners.ChunkListener;
import dev.frankheijden.insights.listeners.EntityListener;
import dev.frankheijden.insights.listeners.PaperBlockListener;
import dev.frankheijden.insights.listeners.PaperEntityListener;
import dev.frankheijden.insights.listeners.PistonListener;
import dev.frankheijden.insights.listeners.PlayerListener;
import dev.frankheijden.insights.listeners.WorldListener;
import dev.frankheijden.minecraftreflection.MinecraftReflection;
import io.papermc.lib.PaperLib;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
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
    private PlayerListener playerListener;

    public ListenerManager(Insights plugin) {
        this.plugin = plugin;
    }

    @Override
    public void register() {
        List<InsightsListener> listeners = new ArrayList<>();
        playerListener = new PlayerListener(plugin);
        listeners.add(playerListener);
        listeners.add(new ChunkListener(plugin));

        List<InsightsListener> disableListeners = new ArrayList<>();
        disableListeners.add(new BlockListener(plugin));
        disableListeners.add(new WorldListener(plugin));

        if (PaperLib.isPaper()) {
            listeners.add(new PaperEntityListener(plugin));
            disableListeners.add(new PaperBlockListener(plugin));
        } else {
            listeners.add(new EntityListener(plugin));
        }

        if (plugin.getSettings().APPLY_PISTON_LIMITS) {
            listeners.add(new PistonListener(plugin));
        }

        listeners.forEach(listener -> plugin.getServer().getPluginManager().registerEvents(listener, plugin));
        disableListeners.forEach(listener -> plugin.getServer().getPluginManager().registerEvents(listener, plugin));

        for (Class<?> clazz : plugin.getSettings().DISABLED_EVENTS) {
            HandlerList list = MinecraftReflection.of(clazz).invoke(null, "getHandlerList");
            for (InsightsListener listener : disableListeners) {
                list.unregister(listener);
            }
            plugin.getLogger().info("Unregistered listener of '" + clazz.getSimpleName() + "'");
        }

        for (Map.Entry<Class<? extends Event>, EventPriority> e : plugin.getSettings().LISTENER_PRIORITIES.entrySet()) {
            if (e.getValue() == EventPriority.LOWEST) continue;

            HandlerList list = MinecraftReflection.of(e.getKey()).invoke(null, "getHandlerList");

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
