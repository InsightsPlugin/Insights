package net.frankheijden.insights.hooks;

import net.frankheijden.insights.Insights;
import net.frankheijden.insights.api.entities.Hook;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

public class HookManager {
    private final Insights plugin;
    private final List<Hook> hooks;

    public HookManager(Insights plugin) {
        this.plugin = plugin;
        this.hooks = new ArrayList<>();
    }

    public void addHook(Hook hook) {
        hooks.add(hook);
    }

    public void removeHook(Hook hook) {
        hooks.remove(hook);
    }

    public List<Hook> getHooks() {
        return hooks;
    }

    public boolean tryHook(String plugin, Hook hook) {
        if (canHook(plugin)) {
            addHook(hook);
            Bukkit.getLogger().info("[Insights] Successfully hooked into " + plugin + "!");
            return true;
        }
        return false;
    }

    public boolean canHook(String plugin) {
        return Bukkit.getPluginManager().getPlugin(plugin) != null;
    }

    public boolean shouldCancel(Block block) {
        for (Hook hook : hooks) {
            if (hook.shouldCancel(block)) return true;
        }
        return false;
    }
}
