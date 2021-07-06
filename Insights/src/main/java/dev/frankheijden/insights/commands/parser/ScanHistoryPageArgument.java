package dev.frankheijden.insights.commands.parser;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.config.Messages;
import dev.frankheijden.insights.api.utils.StringUtils;
import dev.frankheijden.insights.commands.CommandScanHistory;
import io.leangen.geantyref.TypeToken;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;

public class ScanHistoryPageArgument extends CommandArgument<CommandSender, CommandScanHistory.Page> {

    /**
     * Constructs a CommandScanHistory.Page argument.
     */
    public ScanHistoryPageArgument(
            boolean required,
            String name,
            BiFunction<CommandContext<CommandSender>, String, List<String>> suggestionsProvider,
            ArgumentDescription defaultDescription
    ) {
        super(
                required,
                name,
                new ScanHistoryPageParser(),
                "",
                new TypeToken<CommandScanHistory.Page>() {},
                suggestionsProvider,
                defaultDescription
        );
    }

    public static final class ScanHistoryPageParser implements ArgumentParser<CommandSender, CommandScanHistory.Page> {

        @Override
        public ArgumentParseResult<CommandScanHistory.Page> parse(
                CommandContext<CommandSender> cxt,
                Queue<String> inputQueue
        ) {
            if (inputQueue.isEmpty()) {
                return ArgumentParseResult.failure(new NoInputProvidedException(
                        ScanHistoryPageArgument.ScanHistoryPageParser.class,
                        cxt
                ));
            }

            try {
                var pageNumber = Integer.parseInt(inputQueue.peek());
                if (pageNumber <= 0) throw new NumberFormatException();

                inputQueue.poll();
                return ArgumentParseResult.success(new CommandScanHistory.Page(pageNumber - 1));
            } catch (NumberFormatException ex) {
                return ArgumentParseResult.failure(new IllegalArgumentException(
                        "Invalid Page '" + inputQueue.peek() + "'"
                ));
            }
        }

        @Override
        public boolean isContextFree() {
            return true;
        }

        @Override
        public List<String> suggestions(CommandContext<CommandSender> context, String input) {
            if (context.getSender() instanceof Player) {
                var scanHistory = InsightsPlugin.getInstance().getScanHistory();
                int pages = scanHistory.getHistory(((Player) context.getSender()).getUniqueId())
                        .map(Messages.PaginatedMessage::getPageAmount)
                        .orElse(0);

                List<String> suggestions = new ArrayList<>(pages);
                for (var i = 1; i <= pages; i++) {
                    suggestions.add(String.valueOf(i));
                }

                return StringUtils.findThatStartsWith(suggestions, input);
            }

            return Collections.emptyList();
        }
    }
}
