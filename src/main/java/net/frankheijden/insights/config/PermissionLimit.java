package net.frankheijden.insights.config;

import net.frankheijden.insights.utils.YamlUtils;

import java.util.Map;

public class PermissionLimit extends AbstractLimit {

    public PermissionLimit(String name, String permission, Map<String, Integer> materials, Map<String, Integer> entities) {
        super(name, permission);

        this.setMaterials(materials);
        this.setEntities(entities);
    }

    public static PermissionLimit from(YamlUtils utils, String path) {
        String name = utils.getString(YamlUtils.getPath(path, "name"), "");
        String permission = utils.getString(YamlUtils.getPath(path, "permission"), "");
        Map<String, Integer> materials = utils.getMap(YamlUtils.getPath(path, "materials"));
        Map<String, Integer> entities = utils.getMap(YamlUtils.getPath(path, "entities"));
        if (materials.isEmpty() && entities.isEmpty()) {
            return null;
        }
        return new PermissionLimit(name, permission, materials, entities);
    }
}
