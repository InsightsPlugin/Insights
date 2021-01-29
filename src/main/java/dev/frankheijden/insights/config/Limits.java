package dev.frankheijden.insights.config;

import dev.frankheijden.insights.utils.CaseInsensitiveHashMap;
import dev.frankheijden.insights.utils.YamlUtils;
import dev.frankheijden.insights.utils.MessageUtils;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Limits {

    private List<String> PRIORITIES;
    private static final Set<String> PRIORITIES_VALUES = Config.of("GROUPS", "PERMISSIONS", "MATERIALS", "ENTITIES");

    private Map<String, Integer> SINGLE_MATERIALS;
    private Map<String, Integer> SINGLE_ENTITIES;

    private List<GroupLimit> GROUP_LIMITS;
    private List<PermissionLimit> PERMISSION_LIMITS;

    public void reload(YamlUtils utils) {
        this.PRIORITIES = utils.getStringList("general.limits.priorities", PRIORITIES_VALUES, "priority");
        this.SINGLE_MATERIALS = utils.getMap("general.limits.materials", new CaseInsensitiveHashMap<>());
        this.SINGLE_ENTITIES = utils.getMap("general.limits.entities", new CaseInsensitiveHashMap<>());
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

    public Limit getLimit(List<? extends AbstractLimit> list, String str, CommandSender sender) {
        Limit limit = null;
        for (AbstractLimit abstractLimit : list) {
            Integer l = abstractLimit.getLimit(str);
            if (l != null) {
                if (sender.hasPermission(abstractLimit.getPermission())) continue;
                limit = new Limit(abstractLimit.getName(), abstractLimit.getPermission(), l,
                        abstractLimit.getMaterials(str),
                        abstractLimit.getEntities(str));
                break;
            }
        }

        return limit;
    }

    public Limit getLimit(String str, CommandSender sender) {
        Limit limit = null;
        for (String priorityValue : this.PRIORITIES) {
            switch (priorityValue.toLowerCase()) {
                case "materials":
                    Integer lm = this.SINGLE_MATERIALS.get(str);
                    if (lm != null) {
                        String permission = "insights.bypass." + str;
                        if (sender.hasPermission(permission)) continue;
                        limit = new Limit(MessageUtils.getCustomName(str), permission,
                                lm, Collections.singleton(str), null);
                    }
                    break;
                case "entities":
                    Integer le = this.SINGLE_ENTITIES.get(str);
                    if (le != null) {
                        String permission = "insights.bypass." + str;
                        if (sender.hasPermission(permission)) continue;
                        limit = new Limit(MessageUtils.getCustomName(str), permission,
                                le, null, Collections.singleton(str));
                    }
                    break;
                case "groups":
                    limit = this.getLimit(this.GROUP_LIMITS, str, sender);
                    break;
                case "permissions":
                    limit = this.getLimit(this.PERMISSION_LIMITS, str, sender);
                    break;
            }

            if (limit != null) break;
        }

        return limit;
    }
}
