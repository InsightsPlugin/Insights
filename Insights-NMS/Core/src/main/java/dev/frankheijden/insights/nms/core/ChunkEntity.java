package dev.frankheijden.insights.nms.core;

import org.bukkit.entity.EntityType;

public record ChunkEntity(EntityType type, int x, int y, int z) {

}
