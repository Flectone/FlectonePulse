package net.flectone.pulse.platform.registry.cloud;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import net.flectone.pulse.model.entity.FPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.Suggestions;
import org.incendo.cloud.util.StringUtils;

import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;

public class PaperAsyncTabCompleteListener implements Listener {

    private final PlatformCommandManager<CommandSender> manager;
    private final Predicate<String> commandPredicate;
    private final String namespace;

    public PaperAsyncTabCompleteListener(Plugin plugin, PlatformCommandManager<CommandSender> manager, Predicate<String> commandPredicate) {
        this.manager = manager;
        this.commandPredicate = commandPredicate;
        this.namespace = plugin.getName().toLowerCase(Locale.ROOT) + ":";
    }

    @EventHandler
    public void onAsyncTabCompleteEvent(AsyncTabCompleteEvent event) {
        if (!event.isCommand()) return;

        String input = event.getBuffer().startsWith("/") ? event.getBuffer().substring(1) : event.getBuffer();
        if (input.isBlank()) return;

        String label = input.split(" ", 2)[0].toLowerCase(Locale.ROOT);
        if (!commandPredicate.test(label)) return;

        if (label.startsWith(namespace)) {
            input = input.substring(namespace.length());
        }

        FPlayer fPlayer = manager.senderMapper().map(event.getSender());

        Suggestions<FPlayer, ? extends Suggestion> suggestions = manager.suggestionFactory().suggestImmediately(fPlayer, input);
        event.setCompletions(suggestions.list().stream()
                .map(suggestion -> StringUtils.trimBeforeLastSpace(suggestion.suggestion(), suggestions.commandInput()))
                .filter(Objects::nonNull)
                .toList()
        );

        event.setHandled(true);
    }

}
