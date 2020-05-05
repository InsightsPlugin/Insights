package net.frankheijden.insights.managers;

import net.frankheijden.insights.entities.*;
import org.bukkit.Location;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.IslandsManager;

public class BentoBoxCache extends CacheAssistant {

    private final IslandsManager manager = BentoBox.getInstance().getIslands();

    public Selection adapt(Island is) {
        return new Selection(is.getWorld(), is.getProtectionBoundingBox());
    }

    @Override
    public Selection getSelection(Location location) {
        return manager.getIslandAt(location)
                .map(this::adapt)
                .orElse(null);
    }
}
