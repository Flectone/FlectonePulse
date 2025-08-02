package net.flectone.pulse.module.integration.miniplaceholders;

import io.github.miniplaceholders.api.MiniPlaceholders;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// only for modern paper based servers
public class MiniPlaceholdersIntegration implements FIntegration, PulseListener {

    private final Pattern bracesPattern = Pattern.compile("\\{([^}]*)}");

    private final FLogger fLogger;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public MiniPlaceholdersIntegration(FLogger fLogger) {
        this.fLogger = fLogger;
    }

    @Override
    public void hook() {
        fLogger.info("✔ MiniPlaceholders hooked");
    }

    @Override
    public void unhook() {
        fLogger.info("✖ MiniPlaceholders unhooked");
    }

    @Pulse(priority = Event.Priority.HIGH)
    public void onMessageProcessingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.getContext();

        Set<TagResolver> resolvers = new HashSet<>();
        resolvers.add(MiniPlaceholders.getGlobalPlaceholders());

        Player sender = Bukkit.getPlayer(messageContext.getSender().getUuid());
        if (sender != null) {
            try {
                resolvers.add(MiniPlaceholders.getAudiencePlaceholders(sender));

                Player receiver = Bukkit.getPlayer(messageContext.getReceiver().getUuid());
                if (receiver == null) {
                    receiver = sender;
                }

                resolvers.add(MiniPlaceholders.getRelationalPlaceholders(sender, receiver));

            } catch (ClassCastException e) {
                fLogger.warning(e);
            }
        }

        TagResolver[] resolversArray = resolvers.toArray(new TagResolver[0]);
        String message = replaceMiniPlaceholders(messageContext.getMessage(), resolversArray);

        messageContext.setMessage(message);
    }

    private String replaceMiniPlaceholders(String text, TagResolver[] resolvers) {
        Matcher matcher = bracesPattern.matcher(text);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String content = matcher.group(1);

            Component parsedMessage = miniMessage.deserialize(content, resolvers);

            // fix colors problems for custom RP
            // https://github.com/BertTowne/InlineHeads
            matcher.appendReplacement(result, miniMessage.serialize(parsedMessage).replaceAll("</#[0-9a-fA-F]+>", ""));
        }

        matcher.appendTail(result);
        return result.toString();
    }
}
