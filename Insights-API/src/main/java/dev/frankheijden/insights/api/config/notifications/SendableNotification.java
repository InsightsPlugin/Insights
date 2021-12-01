package dev.frankheijden.insights.api.config.notifications;

import dev.frankheijden.insights.api.config.Messages;
import net.kyori.adventure.text.minimessage.Template;

public abstract class SendableNotification {

    protected Messages.Message content;

    protected SendableNotification(Messages.Message content) {
        this.content = content;
    }

    public SendableNotification addTemplates(Template... templates) {
        this.content.addTemplates(templates);
        return this;
    }

    public abstract void send();
}
