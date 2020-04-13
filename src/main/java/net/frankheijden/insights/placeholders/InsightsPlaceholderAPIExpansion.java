package net.frankheijden.insights.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.frankheijden.insights.Insights;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class InsightsPlaceholderAPIExpansion extends PlaceholderExpansion {
    private Insights plugin;

    public InsightsPlaceholderAPIExpansion(Insights plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "Insights";
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getIdentifier() {
        return "insights";
    }

    @Override
    public String getAuthor() {
        return "FrankHeijden";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onRequest(OfflinePlayer player, String identifier) {
        UUID uuid = player.getUniqueId();

        switch (identifier.toUpperCase()) {
            case "SCAN_PROGRESS": {
                Double scanProgress = plugin.getInsightsAPI().getScanProgress(uuid);
                return scanProgress == null ? "" : String.format("%.2f", scanProgress*100) + "%";
            }
            case "SCAN_TIME": {
                String scanTime = plugin.getInsightsAPI().getTimeElapsedOfScan(uuid);
                return scanTime == null ? "" : scanTime;
            }
            default: {
                return null;
            }
        }
    }
}
