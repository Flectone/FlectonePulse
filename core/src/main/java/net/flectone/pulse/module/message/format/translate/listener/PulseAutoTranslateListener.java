package net.flectone.pulse.module.message.format.translate.listener;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.MessagePrepareEvent;
import net.flectone.pulse.model.event.message.MessageSendEvent;
import net.flectone.pulse.model.util.Destination;
import net.flectone.pulse.module.message.format.translate.TranslateModule;
import net.flectone.pulse.module.message.format.translate.model.TranslatedMessage;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.logging.FLogger;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Drives auto-translation of chat messages.
 *
 * <p>Flow:
 * <ol>
 *   <li>{@link MessagePrepareEvent}: kick off async translations for every online
 *       locale that differs from the sender's. The {@link TranslatedMessage} is
 *       cached by message UUID so the per-receiver {@link MessageSendEvent} can
 *       attach it to the receiver's history.</li>
 *   <li>{@link MessageSendEvent}: the original formatted component has already
 *       gone to the receiver — record it into TranslateModule's own history,
 *       attaching the {@link TranslatedMessage}. When a translation lands, the
 *       module will redraw the chat for receivers in that locale.</li>
 * </ol>
 *
 * <p>Self-contained: no reference to DeleteModule. TranslateModule owns its own
 * history + replay mechanism by analogy with DeleteModule but independent.
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PulseAutoTranslateListener implements PulseListener {

    private final TranslateModule translateModule;
    private final FLogger fLogger;

    /** Short-lived bridge from PrepareEvent (per-message) to SendEvent (per-receiver). */
    private final Cache<UUID, TranslatedMessage> preparedTranslations = CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build();

    @Pulse(priority = Event.Priority.HIGH)
    public void onMessagePrepareEvent(MessagePrepareEvent event) {
        EventMetadata<?> metadata = event.eventMetadata();
        UUID messageUUID = metadata.uuid();

        if (metadata.destination().type() != Destination.Type.CHAT) return;

        String message = metadata.message();
        if (message == null || message.isEmpty()) return;

        FEntity senderEntity = metadata.sender();
        if (!(senderEntity instanceof FPlayer sender)) return;

        String senderLocale = sender.getSetting(SettingText.LOCALE);
        if (senderLocale == null) senderLocale = "en_us";

        fLogger.info("[AutoTranslate] PrepareEvent: uuid=%s sender=%s senderLocale=%s message='%s'",
                messageUUID, sender.name(), senderLocale, message);

        TranslatedMessage translatedMessage = translateModule.translateToAllLocales(message, senderLocale);
        if (translatedMessage == null) {
            fLogger.info("[AutoTranslate] PrepareEvent: uuid=%s — nothing to translate (single locale or module disabled)",
                    messageUUID);
            return;
        }

        preparedTranslations.put(messageUUID, translatedMessage);
    }

    @Pulse(priority = Event.Priority.HIGH)
    public MessageSendEvent onMessageSendEvent(MessageSendEvent event) {
        if (event.eventMetadata().destination().type() != Destination.Type.CHAT) return event;

        UUID messageUUID = event.eventMetadata().uuid();
        FPlayer receiver = event.receiver();

        TranslatedMessage translatedMessage = preparedTranslations.getIfPresent(messageUUID);
        if (translatedMessage == null) return event;

        // Save the formatted component plus the raw player text — Component.replaceText
        // uses the raw text as the literal to swap when a translation lands.
        String originalText = event.eventMetadata().message();
        if (originalText == null) originalText = "";
        translateModule.save(receiver, messageUUID, event.message(), originalText, translatedMessage, true);

        return event;
    }
}
