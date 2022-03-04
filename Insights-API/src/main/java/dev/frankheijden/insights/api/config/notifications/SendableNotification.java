package dev.frankheijden.insights.api.config.notifications;

import dev.frankheijden.insights.api.config.Messages;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public abstract class SendableNotification {

    protected Messages.Message content;

    protected SendableNotification(Messages.Message content) {
        this.content = content;
    }

    public SendableNotification addTemplates(TagResolver resolver) {
        this.content.addTemplates(resolver);
        return this;
    }

    public SendableNotification addTemplates(TagResolver... resolvers) {
        this.content.addTemplates(resolvers);
        return this;
    }

    public abstract void send();
}
