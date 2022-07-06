package dev.frankheijden.insights.placeholders;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.region.Region;
import dev.frankheijden.insights.api.concurrent.storage.Storage;
import dev.frankheijden.insights.api.config.limits.Limit;
import dev.frankheijden.insights.api.objects.wrappers.ScanObject;
import dev.frankheijden.insights.api.util.Triplet;
import dev.frankheijden.insights.api.utils.StringUtils;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import java.util.Locale;

public class InsightsPlaceholderExpansion extends PlaceholderExpansion {

    private final InsightsPlugin plugin;

    public InsightsPlaceholderExpansion(InsightsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NonNull String getIdentifier() {
        return "insights";
    }

    @Override
    public @NonNull String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public @NonNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, @NonNull String identifier) {
        if (identifier == null) return ""; // Legacy check
        String[] args = identifier.split("_");
        switch (args[0].toLowerCase(Locale.ENGLISH)) {
            case "limits" -> {
                if (args.length < 3) break;
                String itemString = StringUtils.join(args, "_", 2).toUpperCase(Locale.ENGLISH);
                final ScanObject<?> item;
                try {
                    item = ScanObject.parse(itemString);
                } catch (IllegalArgumentException ex) {
                    return "";
                }

                Triplet<Region, Limit, Storage> smallestLimit = plugin.limits().smallestLimit(
                        player,
                        plugin.regionManager().regionsAt(player.getLocation()),
                        item,
                        0
                );
                if (smallestLimit == null) break;
                switch (args[1].toLowerCase(Locale.ENGLISH)) {
                    case "name" -> {
                        return smallestLimit.b().limitInfo(item).name();
                    }
                    case "max" -> {
                        return String.valueOf(smallestLimit.b().limitInfo(item).limit());
                    }
                    case "count" -> {
                        return String.valueOf(smallestLimit.c().count(smallestLimit.b(), item));
                    }
                    default -> {
                        // Nothing
                    }
                }
            }
            default -> {
                // Nothing
            }
        }
        return "";
    }
}
