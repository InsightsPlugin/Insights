package dev.frankheijden.insights.api.objects.math;

import org.bukkit.World;

public record Cuboid(World world, Vector3 min, Vector3 max) {

}
