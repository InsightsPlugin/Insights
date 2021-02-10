package dev.frankheijden.insights.api.config.notifications;

import dev.frankheijden.insights.api.utils.PlayerUtils;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ActionBarNotification implements Notification {

    protected String content;
    protected final Map<UUID, Player> receivers;

    protected ActionBarNotification(String content) {
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
        return new SendableNotification(content) {
            @Override
            public void send() {
                PlayerUtils.sendActionBar(receivers.values(), content);
            }
        };
    }

    @Override
    public void clear() {
        // Actionbar clears itself
    }
}
