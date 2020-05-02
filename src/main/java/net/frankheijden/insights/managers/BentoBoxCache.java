package net.frankheijden.insights.managers;

import net.frankheijden.insights.entities.*;
import org.bukkit.Location;
import org.bukkit.World;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.IslandsManager;

public class BentoBoxCache extends Cache {

    private final IslandsManager manager = BentoBox.getInstance().getIslands();

    public Selection adapt(Island is) {
        World w = is.getWorld();
        return new Selection(
                new Location(w, is.getMinX(), 0, is.getMinZ()),
                new Location(w, is.getMaxX(), w.getMaxHeight() - 1, is.getMaxZ())
        );
    }

    @Override
    public Selection getSelection(Location location) {
        return manager.getIslandAt(location)
                .map(this::adapt)
                .orElse(null);
    }
}
