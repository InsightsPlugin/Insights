package net.frankheijden.insights.config;

import net.frankheijden.insights.utils.YamlUtils;

import java.util.*;

public class Limits {

    private List<String> PRIORITIES;
    private static final Set<String> PRIORITIES_VALUES = Config.of("GROUPS", "PERMISSIONS", "MATERIALS", "ENTITIES");

    private Map<String, Integer> SINGLE_MATERIALS;
    private Map<String, Integer> SINGLE_ENTITIES;

    private List<GroupLimit> GROUP_LIMITS;
    private List<PermissionLimit> PERMISSION_LIMITS;

    public void reload(YamlUtils utils) {
        this.PRIORITIES = utils.getStringList("general.limits.priorities", PRIORITIES_VALUES, "priority");
        this.SINGLE_MATERIALS = utils.getMap("general.limits.materials");
        this.SINGLE_ENTITIES = utils.getMap("general.limits.entities");
        this.GROUP_LIMITS = new ArrayList<>();
        this.PERMISSION_LIMITS = new ArrayList<>();

        String materialGroupPath = "general.limits.groups";
        Set<String> materialGroups = utils.getKeys(materialGroupPath);
        for (String materialGroup : materialGroups) {
            String path = YamlUtils.getPath(materialGroupPath, materialGroup);
            GroupLimit limit = GroupLimit.from(utils, path);
            if (limit != null) {
                this.GROUP_LIMITS.add(limit);
            }
        }

        String permissionGroupPath = "general.limits.permissions";
        Set<String> permissionGroups = utils.getKeys(permissionGroupPath);
        for (String permissionGroup : permissionGroups) {
            String path = YamlUtils.getPath(permissionGroupPath, permissionGroup);
            PermissionLimit limit = PermissionLimit.from(utils, path);
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
                        abstractLimit.getMaterials(),
                        abstractLimit.getEntities());
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
                                lm, Collections.singleton(str), null);
                    }
                    break;
                case "entities":
                    Integer le = this.SINGLE_ENTITIES.get(str);
                    if (le != null) {
                        String permission = "insights.bypass." + str;
                        limit = new Limit(str, permission,
                                le, Collections.singleton(str), null);
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
