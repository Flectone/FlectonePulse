package net.flectone.pulse.module.integration.miniplaceholders;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.miniplaceholders.api.MiniPlaceholders;
import io.github.miniplaceholders.api.types.RelationalAudience;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class MiniPlaceholdersIntegration implements FIntegration, PulseListener {

    private final Pattern bracesPattern = Pattern.compile("\\{([^}]*)}");

    private final FLogger fLogger;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final PlatformPlayerAdapter platformPlayerAdapter;

    @Inject
    public MiniPlaceholdersIntegration(FLogger fLogger,
                                       PlatformPlayerAdapter platformPlayerAdapter) {
        this.fLogger = fLogger;
        this.platformPlayerAdapter = platformPlayerAdapter;
    }

    @Override
    public void hook() {
        fLogger.info("✔ MiniPlaceholders hooked");
    }

    @Async(delay = 20)
    public void hookLater() {
        hook();
    }

    @Override
    public void unhook() {
        fLogger.info("✖ MiniPlaceholders unhooked");
    }

    @Pulse(priority = Event.Priority.HIGH)
    public void onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.getContext();
        if (messageContext.isFlag(MessageFlag.USER_MESSAGE)) return;

        Set<TagResolver> resolvers = new HashSet<>();
        resolvers.add(MiniPlaceholders.globalPlaceholders());

        Audience sender = getAudienceOrDefault(messageContext.getSender().getUuid(), null);
        Audience receiver = null;
        if (sender != null) {
            receiver = getAudienceOrDefault(messageContext.getReceiver().getUuid(), sender);

            resolvers.add(MiniPlaceholders.audiencePlaceholders());
            resolvers.add(MiniPlaceholders.relationalPlaceholders());
        }

        TagResolver[] resolversArray = resolvers.toArray(new TagResolver[0]);
        String message = replaceMiniPlaceholders(messageContext.getMessage(), resolversArray, sender, receiver);

        messageContext.setMessage(message);
    }

    private Audience getAudienceOrDefault(UUID uuid, Audience defaultAudience) {
        Audience audience = (Audience) platformPlayerAdapter.convertToPlatformPlayer(uuid);
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
