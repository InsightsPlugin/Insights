package net.frankheijden.insights.interfaces;

import net.frankheijden.insights.entities.Area;
import org.bukkit.Location;

public interface Selectable {

    Area getArea(Location location);

}
