package dev.frankheijden.insights.api.config.notifications;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.config.Messages;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ActionBarNotification implements Notification {

    protected InsightsPlugin plugin;
    protected Messages.Message content;
    protected final Map<UUID, Player> receivers;

    protected ActionBarNotification(InsightsPlugin plugin, Messages.Message content) {
        this.plugin = plugin;
        this.content = content;
        this.receivers = new HashMap<>();
    }

    @Override
    public ActionBarNotification add(Player player) {
        receivers.put(player.getUniqueId(), player);
        return this;
    }

    @Override
    public SendableNotification create() {
        return new SendableNotification(content.resetTemplates()) {
            @Override
            public void send() {
                var audiences = plugin.getMessages().getAudiences();
                content.toComponent().ifPresent(component -> receivers.values()
                        .forEach(player -> audiences.player(player).sendActionBar(component)));
            }
        };
    }

    @Override
    public void clear() {
        // Actionbar clears itself
    }
}
