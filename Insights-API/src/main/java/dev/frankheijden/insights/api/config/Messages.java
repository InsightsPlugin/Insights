package dev.frankheijden.insights.api.config;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.concurrent.storage.Storage;
import dev.frankheijden.insights.api.config.parser.PassiveYamlParser;
import dev.frankheijden.insights.api.config.parser.YamlParser;
import dev.frankheijden.insights.api.objects.wrappers.ScanObject;
import dev.frankheijden.insights.api.utils.EnumUtils;
import dev.frankheijden.insights.api.utils.PlayerUtils;
import dev.frankheijden.insights.api.utils.StringUtils;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class Messages {

    private final InsightsPlugin plugin;
    private final BukkitAudiences audiences;
    private final YamlParser parser;

    protected Messages(InsightsPlugin plugin, BukkitAudiences audiences, YamlParser parser) {
        this.plugin = plugin;
        this.audiences = audiences;
        this.parser = parser;
    }

    public Message getMessage(Key messageKey) {
        return getMessage(messageKey.getPath());
    }

    public Message getMessage(String path) {
        return createMessage(parser.getRawString(path));
    }

    /**
     * Creates a paginated message from a DistributionStorage.
     */
    public PaginatedMessage createPaginatedMessage(
            Message header,
            Key formatKey,
            Message footer,
            Storage storage,
            List<ScanObject<?>> keys
    ) {
        List<Component> content = new ArrayList<>(storage.keys().size());

        var serializer = LegacyComponentSerializer.legacyAmpersand();
        for (ScanObject<?> item : keys) {
            var obj = item.getObject();
            var entry = serializer.deserialize(getMessage(formatKey).replace(
                    "entry", EnumUtils.pretty(obj),
                    "count", StringUtils.pretty(storage.count(item))
            ).content);

            if (obj instanceof Material material) {
                var key = material.getKey();
                if (material.isItem()) {
                    entry = entry.hoverEvent(HoverEvent.showItem(
                            net.kyori.adventure.key.Key.key(key.namespace(), key.value()),
                            1
                    ));
                } else {
                    entry = entry.hoverEvent(HoverEvent.showText(Component.text(EnumUtils.pretty(obj))
                            .append(Component.newline())
                            .append(Component.text(key.asString(), NamedTextColor.DARK_GRAY))));
                }
            } else if (obj instanceof EntityType type) {
                var key = type.getKey();
                entry = entry.hoverEvent(HoverEvent.showText(Component.text("Type: " + EnumUtils.pretty(obj))
                        .append(Component.newline())
                        .append(Component.text(key.asString(), NamedTextColor.DARK_GRAY))));
            }

            content.add(entry);
        }

        return new PaginatedMessage(header, footer, content, plugin.getSettings().PAGINATION_RESULTS_PER_PAGE);
    }

    public void close() {
        this.audiences.close();
    }

    public static Messages load(
            InsightsPlugin plugin,
            BukkitAudiences audiences,
            File file,
            InputStream defaultSettings
    ) throws IOException {
        return new Messages(plugin, audiences, PassiveYamlParser.load(file, defaultSettings));
    }

    public enum Key {
        PREFIX("prefix"),
        UPDATE_AVAILABLE("update-available"),
        CONFIGS_RELOADED("configs-reloaded"),
        AREA_SCAN_STARTED("area-scan-started"),
        AREA_SCAN_QUEUED("area-scan-queued"),
        AREA_SCAN_COMPLETED("area-scan-completed"),
        LIMIT_REACHED("limit-reached"),
        LIMIT_NOTIFICATION("limit-notification"),
        LIMIT_DISALLOWED_PLACEMENT("limit-disallowed-placement"),
        SCAN_START("scan.start"),
        SCAN_ALREADY_SCANNING("scan.already-scanning"),
        SCAN_FINISH_HEADER("scan.finish.header"),
        SCAN_FINISH_FORMAT("scan.finish.format"),
        SCAN_FINISH_FOOTER("scan.finish.footer"),
        SCAN_PROGRESS("scan.progress"),
        SCANREGION_NO_REGION("scanregion.no-region"),
        SCANCACHE_NO_CACHE("scancache.no-cache"),
        SCANCACHE_CLEARED("scancache.cleared"),
        SCANCACHE_RESULT_HEADER("scancache.result.header"),
        SCANCACHE_RESULT_FORMAT("scancache.result.format"),
        SCANCACHE_RESULT_FOOTER("scancache.result.footer"),
        SCANHISTORY_NO_HISTORY("scanhistory.no-history"),
        PAGINATION_BUTTON_LEFT("pagination.button-left"),
        PAGINATION_BUTTON_RIGHT("pagination.button-right"),
        PAGINATION_BUTTON_COLOR_ACTIVE("pagination.button-color-active"),
        PAGINATION_BUTTON_COLOR_INACTIVE("pagination.button-color-inactive"),
        PAGINATION_BUTTON_HOVER("pagination.button-hover"),
        PAGINATION_ENTRY_FORMAT("pagination.entry-format"),
        PAGINATION_FOOTER_FORMAT("pagination.footer-format"),
        PAGINATION_NO_PAGE("pagination.no-page"),
        ;

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
            if (content != null && !content.isEmpty()) {
                if (type == Type.ACTIONBAR && sender instanceof Player) {
                    PlayerUtils.sendActionBar(((Player) sender), content);
                } else {
                    sender.sendMessage(content);
                }
            }
        }
    }

    public class PaginatedMessage {

        private enum ButtonType {
            LEFT(Key.PAGINATION_BUTTON_LEFT),
            RIGHT(Key.PAGINATION_BUTTON_RIGHT),
            ;

            Key key;

            ButtonType(Key key) {
                this.key = key;
            }
        }

        private final Message header;
        private final Message footer;
        private final List<Component> content;
        private final int amountPerPage;

        private PaginatedMessage(Message header, Message footer, List<Component> content, int amountPerPage) {
            this.header = header;
            this.footer = footer;
            this.content = content;
            this.amountPerPage = amountPerPage;
        }

        public int getPageAmount() {
            return (int) Math.ceil((double) this.content.size() / this.amountPerPage);
        }

        private Component createFooter(int page) {
            return LegacyComponentSerializer.legacyAmpersand()
                    .deserialize(getMessage(Key.PAGINATION_FOOTER_FORMAT).replace(
                                "current-page", String.valueOf(page + 1),
                                "page-amount", String.valueOf(getPageAmount())
                    ).content)
                    .replaceText(TextReplacementConfig.builder()
                            .matchLiteral("%button-left%")
                            .replacement(createButton(page, ButtonType.LEFT))
                            .build())
                    .replaceText(TextReplacementConfig.builder()
                            .matchLiteral("%button-right%")
                            .replacement(createButton(page, ButtonType.RIGHT))
                            .build());
        }

        private Component createButton(int page, ButtonType type) {
            var button = Component.empty().toBuilder();

            Key buttonColor;
            if ((type == ButtonType.LEFT && page == 0) || (type == ButtonType.RIGHT && page == getPageAmount() - 1)) {
                buttonColor = Key.PAGINATION_BUTTON_COLOR_INACTIVE;
            } else {
                buttonColor = Key.PAGINATION_BUTTON_COLOR_ACTIVE;

                int clickPage = type == ButtonType.LEFT ? page : page + 2;
                button.hoverEvent(HoverEvent.showText(LegacyComponentSerializer.legacyAmpersand().deserialize(
                        getMessage(Key.PAGINATION_BUTTON_HOVER).replace(
                                "page", String.valueOf(clickPage)
                        ).content)
                ));
                button.clickEvent(ClickEvent.runCommand("/scanhistory " + clickPage));
            }

            button.append(LegacyComponentSerializer.legacyAmpersand().deserialize(
                    getMessage(buttonColor).content + getMessage(type.key).content
            ));

            return button.build();
        }

        /**
         * Sends a page of the paginated result.
         */
        public void sendTo(CommandSender sender, int page) {
            int offsetStart = this.amountPerPage * page;
            int offsetEnd = Math.min(offsetStart + this.amountPerPage, content.size());

            if (offsetStart >= content.size()) {
                getMessage(Key.PAGINATION_NO_PAGE).color().sendTo(sender);
                return;
            }

            var component = Component.empty().toBuilder();
            var serializer = LegacyComponentSerializer.legacyAmpersand();

            component.append(serializer.deserialize(header.content)).append(Component.newline());

            for (int i = offsetStart; i < offsetEnd; i++) {
                component.append(content.get(i)).append(Component.newline());
            }

            component.append(serializer.deserialize(footer.content)).append(Component.newline());
            component.append(createFooter(page));

            audiences.sender(sender).sendMessage(component);
        }
    }
}
