package net.frankheijden.insights.config;

import net.frankheijden.insights.utils.CaseInsensitiveHashMap;
import net.frankheijden.insights.utils.YamlUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class PermissionLimit extends AbstractLimit {

    public PermissionLimit(String name, String permission, Map<String, Integer> materials, Map<String, Integer> entities) {
        super(name, permission);

        this.setMaterials(materials);
        this.setEntities(entities);
    }

    public static PermissionLimit from(YamlUtils utils, String path) {
        String name = utils.getString(YamlUtils.getPath(path, "name"), "");
        String permission = utils.getString(YamlUtils.getPath(path, "permission"), "");
        Map<String, Integer> materials = utils.getMap(YamlUtils.getPath(path, "materials"), new CaseInsensitiveHashMap<>());
        Map<String, Integer> entities = utils.getMap(YamlUtils.getPath(path, "entities"), new CaseInsensitiveHashMap<>());
        if (materials.isEmpty() && entities.isEmpty()) {
            return null;
        }
        return new PermissionLimit(name, permission, materials, entities);
    }

    @Override
    public Set<String> getMaterials(String str) {
        if (!super.getMaterials(str).contains(str)) return Collections.emptySet();
        return Collections.singleton(str);
    }

    @Override
    public Set<String> getEntities(String str) {
        if (!super.getEntities(str).contains(str)) return Collections.emptySet();
        return Collections.singleton(str);
    }
}
