package net.frankheijden.insights.config;

import net.frankheijden.insights.utils.YamlUtils;
import org.bukkit.configuration.file.YamlConfiguration;

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

    public static GroupLimit from(YamlConfiguration yml, String path) {
        int limit = yml.getInt(YamlUtils.getPath(path, "limit"), 0);
        if (limit <= 0) {
            return null;
        }

        String name = yml.getString(YamlUtils.getPath(path, "name"), "");
        String permission = yml.getString(YamlUtils.getPath(path, "permission"), null);
        List<String> materials = yml.getStringList(YamlUtils.getPath(path, "materials"));
        List<String> entities = yml.getStringList(YamlUtils.getPath(path, "entities"));

        return new GroupLimit(name, permission, limit, materials, entities);
    }
}
