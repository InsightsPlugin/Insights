package net.frankheijden.insights.utils;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utils {

    public static List<String> SCANNABLE_MATERIALS = Stream.concat(
            Arrays.stream(Material.values())
                    .filter(Material::isBlock)
                    .map(Enum::name),
            Arrays.stream(EntityType.values())
                    .map(Enum::name)
    ).sorted().collect(Collectors.toList());
}
