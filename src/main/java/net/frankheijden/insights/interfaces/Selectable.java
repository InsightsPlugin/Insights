package net.frankheijden.insights.interfaces;

import net.frankheijden.insights.entities.Selection;
import org.bukkit.Location;

public interface Selectable {

    Selection getSelection(Location location);

}
