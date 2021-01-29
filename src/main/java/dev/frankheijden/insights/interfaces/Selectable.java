package dev.frankheijden.insights.interfaces;

import dev.frankheijden.insights.entities.Area;
import org.bukkit.Location;

public interface Selectable {

    Area getArea(Location location);

}
