package dev.frankheijden.insights.tasks;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.events.EntityRemoveFromWorldEvent;
import dev.frankheijden.insights.api.tasks.InsightsTask;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EntityTrackerTask extends InsightsTask {

    private Map<UUID, Entity> trackedEntities;

    public EntityTrackerTask(InsightsPlugin plugin) {
        super(plugin);
        this.trackedEntities = new HashMap<>();
    }

    @Override
    public void run() {
        // Find entities that are alive
        Map<UUID, Entity> entities = new HashMap<>();
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                entities.put(entity.getUniqueId(), entity);
            }
        }

        // Remove all alive entities from the tracked map
        // TrackedEntities now contains the "removed" entities
        trackedEntities.keySet().removeAll(entities.keySet());

        for (Entity entity : trackedEntities.values()) {
            if (entity.isTicking()) {
                Bukkit.getPluginManager().callEvent(new EntityRemoveFromWorldEvent(entity));
            }
        }

        // Update the tracked entities with the scanned entities.
        trackedEntities.clear();
        trackedEntities = entities;
    }
}
