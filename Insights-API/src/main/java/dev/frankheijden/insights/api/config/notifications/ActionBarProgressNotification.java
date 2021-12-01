package dev.frankheijden.insights.api.config.notifications;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.config.Messages;

public class ActionBarProgressNotification extends ActionBarNotification implements ProgressNotification {

    protected final String rawContent;
    protected final int segments;
    protected final String doneColor;
    protected final String totalColor;
    protected final String progressSequence;
    protected final String separator;

    protected ActionBarProgressNotification(
            InsightsPlugin plugin,
            Messages.Message content,
            int segments,
            String doneColor,
            String totalColor,
            String progressSequence,
            String separator
    ) {
        super(plugin, content);
        this.rawContent = content.getRawContent();
        this.segments = segments;
        this.doneColor = doneColor;
        this.totalColor = totalColor;
        this.progressSequence = progressSequence;
        this.separator = separator;
    }

    @Override
    public ActionBarProgressNotification progress(float progress) {
        progress = Math.max(0, Math.min(1, progress));
        int cut = (int) (progress * segments);
        StringBuilder sb = new StringBuilder(segments * progressSequence.length()
                + doneColor.length()
                + totalColor.length()
                + separator.length()
                + rawContent.length()
        );

        sb.append(doneColor);
        for (int i = 0; i < cut; i++) {
            sb.append(progressSequence);
        }
        sb.append(totalColor);
        for (int i = cut; i < segments; i++) {
            sb.append(progressSequence);
        }
        this.content.setRawContent(sb.append(separator).append(rawContent).toString());
        return this;
    }
}
