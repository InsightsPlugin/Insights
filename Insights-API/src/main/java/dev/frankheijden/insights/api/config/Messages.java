package dev.frankheijden.insights.api.config;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.config.parser.PassiveYamlParser;
import dev.frankheijden.insights.api.config.parser.YamlParser;
import dev.frankheijden.insights.api.objects.chunk.ChunkLocation;
import dev.frankheijden.insights.api.objects.wrappers.ScanObject;
import dev.frankheijden.insights.api.utils.StringUtils;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;
import net.kyori.adventure.text.minimessage.template.TemplateResolver;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToLongFunction;

public class Messages {

    private final InsightsPlugin plugin;
    private final BukkitAudiences audiences;
    private final YamlParser parser;
    private final MiniMessage miniMessage;
    private final Map<Key, String> messageCache;
    private final TemplateResolver prefixResolver;

    protected Messages(InsightsPlugin plugin, BukkitAudiences audiences, YamlParser parser) {
        this.plugin = plugin;
        this.audiences = audiences;
        this.parser = parser;
        this.miniMessage = MiniMessage.miniMessage();
        this.messageCache = new EnumMap<>(Key.class);
        this.prefixResolver = TemplateResolver.templates(Template.template(
                "prefix", miniMessage.parse(getRawMessage(Key.PREFIX))
        ));
    }

    public BukkitAudiences getAudiences() {
        return audiences;
    }

    public Message getMessage(Key messageKey) {
        return createMessage(getRawMessage(messageKey));
    }

    public String getRawMessage(Key messageKey) {
        return messageCache.computeIfAbsent(messageKey, k -> parser.getRawString(k.getPath()));
    }

    /**
     * Creates a paginated message from a DistributionStorage.
     */
    public <T> PaginatedMessage<T> createPaginatedMessage(
            Message header,
            Key formatKey,
            Message footer,
            T[] keys,
            ToLongFunction<T> countFunction,
            Function<T, Component> displayNameFunction
    ) {
        return new PaginatedMessage<>(
                header,
                footer,
                keys,
                element -> {
                    Component displayName = displayNameFunction.apply(element);
                    var component = miniMessage.deserialize(getRawMessage(formatKey), TemplateResolver.templates(
                            Template.template("entry", displayName),
                            Template.template("count", StringUtils.pretty(countFunction.applyAsLong(element)))
                    ));
                    return addHover(component, element, displayName);
                },
                plugin.getSettings().PAGINATION_RESULTS_PER_PAGE
        );
    }

    private <T> Component addHover(Component component, T element, Component displayName) {
        if (element instanceof ScanObject<?> scanObject) {
            Object obj = scanObject.getObject();

            if (obj instanceof Material material) {
                var key = material.getKey();
                if (material.isItem()) {
                    return component.hoverEvent(HoverEvent.showItem(
                            net.kyori.adventure.key.Key.key(key.getNamespace(), key.getKey()),
                            1
                    ));
                } else {
                    return component.hoverEvent(HoverEvent.showText(displayName
                            .append(Component.newline())
                            .append(Component.text(key.toString(), NamedTextColor.DARK_GRAY))));
                }
            } else if (obj instanceof EntityType type) {
                var key = type.getKey();
                return component.hoverEvent(HoverEvent.showText(Component.text("Type: ")
                        .append(displayName)
                        .append(Component.newline())
                        .append(Component.text(key.toString(), NamedTextColor.DARK_GRAY))));
            }
        } else if (element instanceof ChunkLocation chunkLoc) {
            var hoverComponent = miniMessage.deserialize(getRawMessage(Key.SCAN_FINISH_CHUNK_HOVER));
            var tpCommand = "/tpchunk " + chunkLoc.getWorld().getName() + ' ' + chunkLoc.getX() + ' ' + chunkLoc.getZ();
            return component
                    .hoverEvent(HoverEvent.showText(hoverComponent))
                    .clickEvent(ClickEvent.runCommand(tpCommand));
        }
        return component;
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
        SCAN_FINISH_CHUNK_FORMAT("scan.finish.chunk-format"),
        SCAN_FINISH_CHUNK_HOVER("scan.finish.chunk-hover"),
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
        PAGINATION_NO_RESULTS("pagination.no-results"),
        STATS("stats"),
        TELEPORTCHUNK_ERROR("teleportchunk.error"),
        TELEPORTCHUNK_NOT_GENERATED("teleportchunk.not-generated"),
        TELEPORTCHUNK_FAILED("teleportchunk.failed"),
        TELEPORTCHUNK_SUCCESS("teleportchunk.success"),
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

        return new Message(str, messageType, prefixResolver);
    }

    public class Message {

        private enum Type {
            ACTIONBAR,
            CHAT
        }

        private String content;
        private final Type type;
        private final TemplateResolver originalResolver;
        private TemplateResolver resolver;

        private Message() {
            this(null, Type.CHAT, TemplateResolver.empty());
        }

        private Message(String content, Type type, TemplateResolver resolver) {
            this.content = content;
            this.type = type;
            this.originalResolver = resolver;
            this.resolver = resolver;
        }

        public Message resetTemplates() {
            this.resolver = originalResolver;
            return this;
        }

        public Message addTemplates(Template... templates) {
            this.resolver = TemplateResolver.combining(resolver, TemplateResolver.templates(templates));
            return this;
        }

        public void setRawContent(String content) {
            this.content = content;
        }

        public String getRawContent() {
            return content;
        }

        public Optional<Component> toComponent() {
            if (content == null || content.isEmpty()) return Optional.empty();
            return Optional.of(miniMessage.deserialize(content, resolver));
        }

        /**
         * Sends the message to given receiver, using the message type defined.
         */
        public void sendTo(CommandSender sender) {
            sendTo(audiences.sender(sender));
        }

        /**
         * Sends the message to given audience, using the message type defined.
         */
        public void sendTo(Audience audience) {
            if (content != null && !content.isEmpty()) {
                var component = miniMessage.deserialize(content, resolver);
                if (type == Type.ACTIONBAR) {
                    audience.sendActionBar(component);
                } else {
                    audience.sendMessage(component);
                }
            }
        }
    }

    public class PaginatedMessage<T> {

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
        private final T[] elements;
        private final Function<T, Component> elementFormatFunction;
        private final int amountPerPage;

        private PaginatedMessage(
                Message header,
                Message footer,
                T[] elements,
                Function<T, Component> elementFormatFunction,
                int amountPerPage
        ) {
            this.header = header;
            this.footer = footer;
            this.elements = elements;
            this.elementFormatFunction = elementFormatFunction;
            this.amountPerPage = amountPerPage;
        }

        public int getPageAmount() {
            return (int) Math.ceil((double) this.elements.length / this.amountPerPage);
        }

        private Component createFooter(int page) {
            return miniMessage.deserialize(getRawMessage(Key.PAGINATION_FOOTER_FORMAT), TemplateResolver.templates(
                    Template.template("current-page", String.valueOf(page + 1)),
                    Template.template("page-amount", String.valueOf(getPageAmount())),
                    Template.template("button-left", createButton(page, ButtonType.LEFT)),
                    Template.template("button-right", createButton(page, ButtonType.RIGHT))
            ));
        }

        private Component createButton(int page, ButtonType type) {
            var button = Component.empty().toBuilder();

            Key buttonColor;
            if ((type == ButtonType.LEFT && page == 0) || (type == ButtonType.RIGHT && page == getPageAmount() - 1)) {
                buttonColor = Key.PAGINATION_BUTTON_COLOR_INACTIVE;
            } else {
                buttonColor = Key.PAGINATION_BUTTON_COLOR_ACTIVE;

                int clickPage = type == ButtonType.LEFT ? page : page + 2;
                button.hoverEvent(HoverEvent.showText(miniMessage.deserialize(
                        getRawMessage(Key.PAGINATION_BUTTON_HOVER),
                        TemplateResolver.templates(
                                Template.template("page", String.valueOf(clickPage))
                        )
                )));
                button.clickEvent(ClickEvent.runCommand("/scanhistory " + clickPage));
            }

            button.append(miniMessage.deserialize(getRawMessage(buttonColor) + getRawMessage(type.key)));

            return button.build();
        }

        /**
         * Sends a page of the paginated result.
         */
        public void sendTo(CommandSender sender, int page) {
            if (elements.length == 0) {
                getMessage(Key.PAGINATION_NO_RESULTS).sendTo(sender);
                return;
            }

            int offsetStart = amountPerPage * page;
            int offsetEnd = Math.min(offsetStart + amountPerPage, elements.length);
            if (offsetStart >= elements.length) {
                getMessage(Key.PAGINATION_NO_PAGE).sendTo(sender);
                return;
            }

            List<Component> components = new ArrayList<>(amountPerPage);
            for (var i = offsetStart; i < offsetEnd; i++) {
                components.add(elementFormatFunction.apply(elements[i]));
            }

            var audience = audiences.sender(sender);

            header.sendTo(sender);
            components.forEach(audience::sendMessage);
            footer.sendTo(sender);
            audience.sendMessage(createFooter(page));
        }
    }
}
