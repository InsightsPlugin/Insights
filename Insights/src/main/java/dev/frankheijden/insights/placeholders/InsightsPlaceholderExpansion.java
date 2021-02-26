package dev.frankheijden.insights.placeholders;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.addons.Region;
import dev.frankheijden.insights.api.concurrent.storage.DistributionStorage;
import dev.frankheijden.insights.api.config.LimitEnvironment;
import dev.frankheijden.insights.api.config.limits.Limit;
import dev.frankheijden.insights.api.objects.wrappers.ScanObject;
import dev.frankheijden.insights.api.utils.ChunkUtils;
import dev.frankheijden.insights.api.utils.StringUtils;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class InsightsPlaceholderExpansion extends PlaceholderExpansion {

    private final InsightsPlugin plugin;

    public InsightsPlaceholderExpansion(InsightsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "insights";
    }

    @Override
    public String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (identifier == null) return "";
        String[] args = identifier.split("_");
        switch (args[0].toLowerCase(Locale.ENGLISH)) {
            case "limits":
                if (args.length < 3) break;

                String itemString = StringUtils.join(args, "_", 2).toUpperCase(Locale.ENGLISH);
                final ScanObject<?> item;
                try {
                    item = ScanObject.parse(itemString);
                } catch (IllegalArgumentException ex) {
                    return "";
                }

                Location location = player.getLocation();
                World world = location.getWorld();
                UUID worldUid = world.getUID();
                LimitEnvironment env = new LimitEnvironment(player, world.getName());
                Optional<Limit> limitOptional = plugin.getLimits().getFirstLimit(item, env);
                if (!limitOptional.isPresent()) break;

                Limit limit = limitOptional.get();
                switch (args[1].toLowerCase(Locale.ENGLISH)) {
                    case "name": return limit.getLimit(item).getName();
                    case "max": return String.valueOf(limit.getLimit(item).getLimit());
                    case "count":
                        Optional<Region> regionOptional = plugin.getAddonManager().getRegion(location);
                        Optional<DistributionStorage> storageOptional;
                        if (regionOptional.isPresent()) {
                            Region region = regionOptional.get();
                            storageOptional = plugin.getAddonStorage().get(region.getKey());
                        } else {
                            long chunkKey = ChunkUtils.getKey(location);
                            storageOptional = plugin.getWorldStorage().getWorld(worldUid).get(chunkKey);
                        }
                        return storageOptional.map(storage -> String.valueOf(storage.count(limit, item)))
                                .orElse("");
                    default: break;
                }
                break;
            default: break;
        }
        return "";
    }
}
