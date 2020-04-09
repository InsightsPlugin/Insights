package net.frankheijden.insights.config;

import net.frankheijden.insights.utils.YamlUtils;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Map;

public class PermissionLimit extends AbstractLimit {

    public PermissionLimit(String name, String permission, Map<String, Integer> materials, Map<String, Integer> entities) {
        super(name, permission);

        this.setMaterials(materials);
        this.setEntities(entities);
    }

    public static PermissionLimit from(YamlConfiguration yml, String path) {
        String name = yml.getString(YamlUtils.getPath(path, "name"), "");
        String permission = yml.getString(YamlUtils.getPath(path, "permission"), null);
        Map<String, Integer> materials = YamlUtils.getMap(yml, YamlUtils.getPath(path, "materials"));
        Map<String, Integer> entities = YamlUtils.getMap(yml, YamlUtils.getPath(path, "entities"));
        if (materials.isEmpty() && entities.isEmpty()) {
            return null;
        }
        return new PermissionLimit(name, permission, materials, entities);
    }
}
