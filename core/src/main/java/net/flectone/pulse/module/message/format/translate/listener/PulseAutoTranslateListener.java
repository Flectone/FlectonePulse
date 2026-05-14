package net.flectone.pulse.module.message.format.translate.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.MessagePrepareEvent;
import net.flectone.pulse.model.event.message.MessageSendEvent;
import net.flectone.pulse.model.util.Destination;
import net.flectone.pulse.module.message.format.moderation.delete.DeleteModule;
import net.flectone.pulse.module.message.format.translate.TranslateModule;
import net.flectone.pulse.module.message.format.translate.model.TranslatedMessage;
import net.flectone.pulse.util.constant.SettingText;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PulseAutoTranslateListener implements PulseListener {

    private final TranslateModule translateModule;
    private final DeleteModule deleteModule;
    private final Cache<UUID, TranslatedMessage> preparedTranslations = CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build();

    @Pulse(priority = Event.Priority.HIGH)
    public void onMessagePrepareEvent(MessagePrepareEvent event) {
        // Only translate chat messages
        EventMetadata<?> metadata = event.eventMetadata();
        if (metadata.destination().type() != Destination.Type.CHAT) return;

        String message = metadata.message();
        if (message == null || message.isEmpty()) return;

        FEntity senderEntity = metadata.sender();
        if (!(senderEntity instanceof FPlayer sender)) return;
        String senderLocale = sender.getSetting(SettingText.LOCALE);
        if (senderLocale == null) senderLocale = "en_us";

        // Translate to all unique locales on server asynchronously
        // Original text is available immediately, translations are added in background
        TranslatedMessage translatedMessage = translateModule.translateToAllLocales(message, senderLocale);
        if (translatedMessage != null) {
            // Store for later use in MessageSendEvent
            preparedTranslations.put(metadata.uuid(), translatedMessage);
        }
    }

    @Pulse(priority = Event.Priority.HIGH)
    public MessageSendEvent onMessageSendEvent(MessageSendEvent event) {
        // Only handle chat messages
        if (event.eventMetadata().destination().type() != Destination.Type.CHAT) return event;

        UUID messageUUID = event.eventMetadata().uuid();
        TranslatedMessage translatedMessage = preparedTranslations.getIfPresent(messageUUID);

        if (translatedMessage == null) return event;

        // Save formatted message to history with translations for toggle functionality.
        // event.message() is the full formatted component (player name + colors + translation button),
        // NOT the raw text. Previously event.withMessage(translatedComponent) was called here which
        // replaced the full format with plain text, causing the white-text-in-chat bug.
        deleteModule.save(event.receiver(), messageUUID, event.message(), translatedMessage, true);

        return event;
    }
}
