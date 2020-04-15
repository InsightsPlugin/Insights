package net.frankheijden.insights.config;

import net.frankheijden.insights.utils.YamlUtils;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.*;

public class Limits {

    private final Config config;

    private List<String> PRIORITIES;

    private Map<String, Integer> SINGLE_MATERIALS;
    private Map<String, Integer> SINGLE_ENTITIES;

    private List<GroupLimit> GROUP_LIMITS;
    private List<PermissionLimit> PERMISSION_LIMITS;

    public Limits(Config config) {
        this.config = config;
    }

    public void reload() {
        YamlConfiguration yml = config.getConfig();
        this.PRIORITIES = yml.getStringList("general.limits.priorities");
        this.SINGLE_MATERIALS = YamlUtils.getMap(yml, "general.limits.materials");
        this.SINGLE_ENTITIES = YamlUtils.getMap(yml, "general.limits.entities");
        this.GROUP_LIMITS = new ArrayList<>();
        this.PERMISSION_LIMITS = new ArrayList<>();

        String materialGroupPath = "general.limits.groups";
        Set<String> materialGroups = YamlUtils.getKeys(yml, materialGroupPath);
        for (String materialGroup : materialGroups) {
            String path = YamlUtils.getPath(materialGroupPath, materialGroup);
            GroupLimit limit = GroupLimit.from(yml, path);
            if (limit != null) {
                this.GROUP_LIMITS.add(limit);
            }
        }

        String permissionGroupPath = "general.limits.permissions";
        Set<String> permissionGroups = YamlUtils.getKeys(yml, permissionGroupPath);
        for (String permissionGroup : permissionGroups) {
            String path = YamlUtils.getPath(permissionGroupPath, permissionGroup);
            PermissionLimit limit = PermissionLimit.from(yml, path);
            if (limit != null) {
                this.PERMISSION_LIMITS.add(limit);
            }
        }
    }

    public Limit getLimit(List<? extends AbstractLimit> list, String str) {
        Limit limit = null;
        for (AbstractLimit abstractLimit : list) {
            Integer l = abstractLimit.getLimit(str);
            if (l != null) {
                limit = new Limit(abstractLimit.getName(), abstractLimit.getPermission(), l,
                        new ArrayList<>(abstractLimit.getMaterials()),
                        new ArrayList<>(abstractLimit.getEntities()));
                break;
            }
        }

        return limit;
    }

    public Limit getLimit(String str) {
        Limit limit = null;
        for (String priorityValue : this.PRIORITIES) {
            switch (priorityValue.toLowerCase()) {
                case "materials":
                    Integer lm = this.SINGLE_MATERIALS.get(str);
                    if (lm != null) {
                        String permission = "insights.bypass." + str;
                        limit = new Limit(str, permission,
                                lm, Collections.singletonList(str), null);
                    }
                    break;
                case "entities":
                    Integer le = this.SINGLE_ENTITIES.get(str);
                    if (le != null) {
                        String permission = "insights.bypass." + str;
                        limit = new Limit(str, permission,
                                le, Collections.singletonList(str), null);
                    }
                    break;
                case "groups":
                    limit = this.getLimit(this.GROUP_LIMITS, str);
                    break;
                case "permissions":
                    limit = this.getLimit(this.PERMISSION_LIMITS, str);
                    break;
            }

            if (limit != null) break;
        }

        return limit;
    }
}
