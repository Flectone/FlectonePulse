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
import net.flectone.pulse.util.logging.FLogger;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PulseAutoTranslateListener implements PulseListener {

    private final TranslateModule translateModule;
    private final DeleteModule deleteModule;
    private final FLogger fLogger;
    private final Cache<UUID, TranslatedMessage> preparedTranslations = CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build();

    @Pulse(priority = Event.Priority.HIGH)
    public void onMessagePrepareEvent(MessagePrepareEvent event) {
        EventMetadata<?> metadata = event.eventMetadata();
        UUID messageUUID = metadata.uuid();

        if (metadata.destination().type() != Destination.Type.CHAT) {
            fLogger.info("[AutoTranslate] PrepareEvent: skip uuid=%s — destination=%s (not CHAT)",
                    messageUUID, metadata.destination().type());
            return;
        }

        String message = metadata.message();
        if (message == null || message.isEmpty()) {
            fLogger.info("[AutoTranslate] PrepareEvent: skip uuid=%s — message is null/empty", messageUUID);
            return;
        }

        FEntity senderEntity = metadata.sender();
        if (!(senderEntity instanceof FPlayer sender)) {
            fLogger.info("[AutoTranslate] PrepareEvent: skip uuid=%s — sender is not FPlayer (%s)",
                    messageUUID, senderEntity == null ? "null" : senderEntity.getClass().getSimpleName());
            return;
        }
        String senderLocale = sender.getSetting(SettingText.LOCALE);
        if (senderLocale == null) senderLocale = "en_us";

        fLogger.info("[AutoTranslate] PrepareEvent: uuid=%s sender=%s senderLocale=%s message='%s'",
                messageUUID, sender.name(), senderLocale, message);

        TranslatedMessage translatedMessage = translateModule.translateToAllLocales(message, senderLocale);
        if (translatedMessage == null) {
            fLogger.info("[AutoTranslate] PrepareEvent: uuid=%s — translateToAllLocales returned null (no translation needed or module disabled)",
                    messageUUID);
            return;
        }

        preparedTranslations.put(messageUUID, translatedMessage);
        fLogger.info("[AutoTranslate] PrepareEvent: uuid=%s — stored TranslatedMessage, initial locales in map=%s",
                messageUUID, translatedMessage.translations().keySet());
    }

    @Pulse(priority = Event.Priority.HIGH)
    public MessageSendEvent onMessageSendEvent(MessageSendEvent event) {
        UUID messageUUID = event.eventMetadata().uuid();
        FPlayer receiver = event.receiver();

        if (event.eventMetadata().destination().type() != Destination.Type.CHAT) {
            fLogger.info("[AutoTranslate] SendEvent: skip uuid=%s — destination=%s (not CHAT)",
                    messageUUID, event.eventMetadata().destination().type());
            return event;
        }

        TranslatedMessage translatedMessage = preparedTranslations.getIfPresent(messageUUID);

        if (translatedMessage == null) {
            fLogger.info("[AutoTranslate] SendEvent: uuid=%s receiver=%s — NO prepared translation (cache miss, message will be shown without translation)",
                    messageUUID, receiver == null ? "null" : receiver.name());
            return event;
        }

        fLogger.info("[AutoTranslate] SendEvent: uuid=%s receiver=%s — prepared translation FOUND, locales=%s, saving to history",
                messageUUID, receiver == null ? "null" : receiver.name(),
                translatedMessage.translations().keySet());

        // Save formatted message to history with translations for toggle functionality.
        // event.message() is the full formatted component (player name + colors + translation button),
        // NOT the raw text. Previously event.withMessage(translatedComponent) was called here which
        // replaced the full format with plain text, causing the white-text-in-chat bug.
        deleteModule.save(receiver, messageUUID, event.message(), translatedMessage, true);

        return event;
    }
}
