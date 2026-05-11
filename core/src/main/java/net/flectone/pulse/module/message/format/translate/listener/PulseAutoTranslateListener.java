package net.flectone.pulse.module.message.format.translate.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
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
import net.kyori.adventure.text.Component;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PulseAutoTranslateListener implements PulseListener {

    private final TranslateModule translateModule;
    private final DeleteModule deleteModule;
    private final ConcurrentHashMap<UUID, TranslatedMessage> preparedTranslations = new ConcurrentHashMap<>();

    @Pulse(priority = Event.Priority.HIGH)
    public void onMessagePrepareEvent(MessagePrepareEvent event) {
        // Only translate chat messages
        EventMetadata<?> metadata = event.eventMetadata();
        if (metadata.destination().type() != Destination.Type.CHAT) return;

        String message = metadata.message();
        if (message == null || message.isEmpty()) return;

        // Get sender locale
        FPlayer sender = (FPlayer) metadata.sender();
        String senderLocale = sender.getSetting(SettingText.LOCALE);
        if (senderLocale == null) senderLocale = "en_us";

        // Translate to all unique locales on server
        TranslatedMessage translatedMessage = translateModule.translateToAllLocales(message, senderLocale);
        if (translatedMessage != null) {
            // Store for later use in MessageSendEvent
            preparedTranslations.put(metadata.uuid(), translatedMessage);
        }
    }

    @Pulse(priority = Event.Priority.MONITOR)
    public void onMessageSendEvent(MessageSendEvent event) {
        // Only handle chat messages
        if (event.eventMetadata().destination().type() != Destination.Type.CHAT) return;

        UUID messageUUID = event.eventMetadata().uuid();
        TranslatedMessage translatedMessage = preparedTranslations.get(messageUUID);

        if (translatedMessage == null) return;

        FPlayer receiver = event.receiver();
        Component message = event.message();

        // Get receiver locale
        String receiverLocale = receiver.getSetting(SettingText.LOCALE);
        if (receiverLocale == null) receiverLocale = "en_us";

        // Get translated component for receiver
        Component translatedComponent = translatedMessage.getTranslation(receiverLocale);

        // Save to history with translations
        deleteModule.save(receiver, messageUUID, translatedComponent, translatedMessage, true);

        // Clean up after all receivers processed
        // Note: This is a simple cleanup, might need improvement for concurrent access
        preparedTranslations.remove(messageUUID);
    }
}
