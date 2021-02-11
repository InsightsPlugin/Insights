package dev.frankheijden.insights.api.config;

import dev.frankheijden.insights.api.config.parser.PassiveYamlParser;
import dev.frankheijden.insights.api.config.parser.YamlParser;
import dev.frankheijden.insights.api.utils.PlayerUtils;
import dev.frankheijden.insights.api.utils.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Optional;

public class Messages {

    private final YamlParser parser;

    protected Messages(YamlParser parser) {
        this.parser = parser;
    }

    public Message getMessage(Key messageKey) {
        return getMessage(messageKey.getPath());
    }

    public Message getMessage(String path) {
        return createMessage(parser.getRawString(path));
    }

    public static Messages load(File file, InputStream defaultSettings) throws IOException {
        return new Messages(PassiveYamlParser.load(file, defaultSettings));
    }

    public enum Key {
        PREFIX("prefix"),
        CONFIGS_RELOADED("configs-reloaded"),
        AREA_SCAN_STARTED("area-scan-started"),
        AREA_SCAN_QUEUED("area-scan-queued"),
        AREA_SCAN_COMPLETED("area-scan-completed"),
        LIMIT_REACHED("limit-reached"),
        LIMIT_NOTIFICATION("limit-notification"),
        SCAN_START("scan.start"),
        SCAN_ALREADY_SCANNING("scan.already-scanning"),
        SCAN_FINISH_HEADER("scan.finish.header"),
        SCAN_FINISH_FORMAT("scan.finish.format"),
        SCAN_FINISH_FOOTER("scan.finish.footer"),
        SCAN_PROGRESS("scan.progress"),
        SCANREGION_NO_REGION("scanregion.no-region");

        private final String path;

        Key(String path) {
            this.path = "messages." + path;
        }

        public String getPath() {
            return path;
        }
    }

    /**
     * Creates a new message based on a string representation of the message type and content.
     * If the message starts with "&lt;TYPE&gt;" (e.g. &lt;actionbar&gt;),
     * the message typed contained in brackets will be used when sending.
     */
    public Message createMessage(String str) {
        if (str == null) return new Message();
        String upper = str.toUpperCase(Locale.ENGLISH);

        Message.Type messageType = Message.Type.CHAT;
        for (Message.Type type : Message.Type.values()) {
            if (upper.startsWith('<' + type.name() + '>')) {
                int offset = 2 + type.name().length();
                str = str.substring(offset);
                upper = upper.substring(offset);
                messageType = type;
                break;
            }
        }

        if (upper.startsWith("<PREFIX>")) {
            str = getMessage(Key.PREFIX).getMessage().orElse("") + str.substring(8);
        }

        return new Message(str, messageType);
    }

    public static final class Message {

        private enum Type {
            ACTIONBAR,
            CHAT
        }

        private String content;
        private Type type;

        private Message() {
            this(null);
        }

        private Message(String content) {
            this(content, Type.CHAT);
        }

        private Message(String content, Type type) {
            this.content = content;
            this.type = type;
        }

        public Message type(Type type) {
            this.type = type;
            return this;
        }

        public Message replace(String... replacements) {
            if (content != null) content = StringUtils.replace(content, replacements);
            return this;
        }

        public Message color() {
            if (content != null) content = ChatColor.translateAlternateColorCodes('&', content);
            return this;
        }

        public Optional<String> getMessage() {
            return Optional.ofNullable(content);
        }

        /**
         * Sends the message to given receiver, using the message type defined.
         */
        public void sendTo(CommandSender sender) {
            if (type == Type.ACTIONBAR && sender instanceof Player) {
                PlayerUtils.sendActionBar(((Player) sender), content);
            } else {
                sender.sendMessage(content);
            }
        }
    }
}
