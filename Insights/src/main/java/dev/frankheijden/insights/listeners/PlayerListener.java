package dev.frankheijden.insights.listeners;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.listeners.InsightsListener;
import dev.frankheijden.insights.api.tasks.UpdateCheckerTask;
import dev.frankheijden.insights.api.utils.BlockUtils;
import dev.frankheijden.insights.api.utils.LocationUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class PlayerListener extends InsightsListener {

    private final Cache<String, ExplodedBed> intentionalDesignBugs = CacheBuilder.newBuilder()
            .expireAfterWrite(500, TimeUnit.MILLISECONDS)
            .build();

    public PlayerListener(InsightsPlugin plugin) {
        super(plugin);
    }

    public Optional<ExplodedBed> getIntentionalDesignBugAt(Location loc) {
        return Optional.ofNullable(this.intentionalDesignBugs.getIfPresent(LocationUtils.getKey(loc)));
    }

    /**
     * Handles the PlayerJoinEvent, updating the concurrent PlayerList and checking for updates.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getPlayerList().addPlayer(player);

        if (player.hasPermission("insights.update")) {
            UpdateCheckerTask.check(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerJoinEvent event) {
        plugin.getPlayerList().removePlayer(event.getPlayer());
    }

    /**
     * Handles the PlayerInteractEvent to track bed explosions in the nether/end.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        // Only need to check beds
        Material material = block.getType();
        if (!Tag.BEDS.isTagged(material)) return;

        // Only need to check this in the nether/end
        if (block.getWorld().getEnvironment() == World.Environment.NORMAL) return;

        BlockUtils.getOtherHalf(block).ifPresent(otherHalf -> {
            Location location = block.getLocation();
            Location otherHalfLocation = otherHalf.getLocation();

            ExplodedBed explodedBed = new ExplodedBed(material, location, otherHalfLocation);
            intentionalDesignBugs.put(LocationUtils.getKey(location), explodedBed);
            intentionalDesignBugs.put(LocationUtils.getKey(otherHalfLocation), explodedBed);
        });
    }

    public static final class ExplodedBed {

        private final Material material;
        private final Location head;
        private final Location foot;

        private ExplodedBed(Material material, Location head, Location foot) {
            this.material = material;
            this.head = head;
            this.foot = foot;
        }

        public Material getMaterial() {
            return material;
        }

        public Location getHead() {
            return head;
        }

        public Location getFoot() {
            return foot;
        }
    }
}
