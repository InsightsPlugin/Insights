package net.frankheijden.insights.config;

import net.frankheijden.insights.utils.YamlUtils;

import java.util.*;

public class GroupLimit extends AbstractLimit {

    public GroupLimit(String name, String permission, Integer limit, List<String> materials, List<String> entities) {
        super(name, permission);

        Map<String, Integer> materialMap = new HashMap<>();
        materials.forEach(m -> materialMap.put(m, limit));
        this.setMaterials(materialMap);

        Map<String, Integer> entityMap = new HashMap<>();
        entities.forEach(e -> entityMap.put(e, limit));
        this.setEntities(entityMap);
    }

    public static GroupLimit from(YamlUtils utils, String path) {
        int limit = utils.getIntWithinRange(YamlUtils.getPath(path, "limit"), 0, null, null);
        if (limit < 0) {
            return null;
        }

        String name = utils.getString(YamlUtils.getPath(path, "name"), "");
        String permission = utils.getString(YamlUtils.getPath(path, "permission"), "");
        List<String> materials = utils.getStringList(YamlUtils.getPath(path, "materials"));
        List<String> entities = utils.getStringList(YamlUtils.getPath(path, "entities"));

        return new GroupLimit(name, permission, limit, materials, entities);
    }
}
