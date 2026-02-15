package net.flectone.pulse.module.integration.miniplaceholders;

import io.github.miniplaceholders.api.MiniPlaceholders;
import io.github.miniplaceholders.api.types.RelationalAudience;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;

import java.util.Set;
import java.util.UUID;
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
    public Event onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.context();
        if (messageContext.isFlag(MessageFlag.USER_MESSAGE)) return event;

        Set<TagResolver> resolvers = new ObjectOpenHashSet<>();
        resolvers.add(MiniPlaceholders.globalPlaceholders());

        FEntity fSender = messageContext.sender();
        FEntity fReceiver = messageContext.receiver();

        // switch parsing
        if (!messageContext.isFlag(MessageFlag.SENDER_INTEGRATION_PLACEHOLDERS)) {
            FEntity tempFPlayer = fSender;
            fSender = fReceiver;
            fReceiver = tempFPlayer;
        }

        Audience sender = getAudienceOrDefault(fSender.uuid(), null);
        Audience receiver = null;

        if (sender != null) {
            receiver = getAudienceOrDefault(fReceiver.uuid(), sender);

            resolvers.add(MiniPlaceholders.audiencePlaceholders());
            resolvers.add(MiniPlaceholders.relationalPlaceholders());
        }

        TagResolver[] resolversArray = resolvers.toArray(new TagResolver[0]);
        String message = replaceMiniPlaceholders(messageContext.message(), resolversArray, sender, receiver);

        return event.withContext(messageContext.withMessage(message));
    }

    private Audience getAudienceOrDefault(UUID uuid, Audience defaultAudience) {
        Audience audience = Bukkit.getPlayer(uuid);
        return audience == null ? defaultAudience : audience;
    }

    private String replaceMiniPlaceholders(String text, TagResolver[] resolvers, Audience sender, Audience receiver) {
        Matcher matcher = bracesPattern.matcher(text);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String content = matcher.group(1);

            Component parsedMessage = sender == null || receiver == null
                    ? miniMessage.deserialize(content, resolvers)
                    : miniMessage.deserialize(content, new RelationalAudience<>(sender, receiver), resolvers);

            // fix colors problems for custom RP
            // https://github.com/BertTowne/InlineHeads
            matcher.appendReplacement(result, miniMessage.serialize(parsedMessage).replaceAll("</#[0-9a-fA-F]+>", ""));
        }

        matcher.appendTail(result);
        return result.toString();
    }
}
