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
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.model.event.message.MessageSendEvent;
import net.flectone.pulse.model.event.player.PlayerQuitEvent;
import net.flectone.pulse.model.util.Destination;
import net.flectone.pulse.module.message.format.translate.TranslateModule;
import net.flectone.pulse.module.message.format.translate.model.TranslatedMessage;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Drives auto-translation of chat plus captures the full chat stream so the
 * replay (triggered on translation arrival or toggle click) restores all
 * messages a player has seen — chat, server announcements, join/quit, etc.
 *
 * <p>Without the full stream, replay would only redraw player chat and push
 * server messages off-screen, since the brute-redraw rewrites the visible
 * chat area from scratch.
 *
 * <p>Self-contained: no reference to DeleteModule.
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PulseAutoTranslateListener implements PulseListener {

    private final TranslateModule translateModule;
    private final FLogger fLogger;

    /** Bridges per-message PrepareEvent to per-receiver SendEvent. */
    private final Cache<UUID, TranslatedMessage> preparedTranslations = CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build();

    /**
     * Dedupes duplicate PrepareEvent fires within 1s for the same (sender, text).
     * On Paper/Purpur the chat pipeline sometimes dispatches twice — without this
     * we'd issue 2 API calls, save 2 history entries, and trigger 2 replays.
     */
    private final Cache<String, Boolean> recentMessages = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.SECONDS)
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

        String dedupKey = sender.uuid() + ":" + senderLocale + ":" + message;
        if (recentMessages.getIfPresent(dedupKey) != null) {
            fLogger.debug("[AutoTranslate] PrepareEvent: skip uuid=%s — duplicate of recent message (sender=%s text='%s')",
                    messageUUID, sender.name(), message);
            return;
        }
        recentMessages.put(dedupKey, Boolean.TRUE);

        fLogger.debug("[AutoTranslate] PrepareEvent: uuid=%s sender=%s senderLocale=%s message='%s'",
                messageUUID, sender.name(), senderLocale, message);

        TranslatedMessage translatedMessage = translateModule.translateToAllLocales(message, senderLocale);
        if (translatedMessage == null) {
            fLogger.debug("[AutoTranslate] PrepareEvent: uuid=%s — nothing to translate (single locale or module disabled)",
                    messageUUID);
            return;
        }

        preparedTranslations.put(messageUUID, translatedMessage);
    }

    /**
     * Per-receiver chat send — record EVERY chat into history, with or without
     * translation. This is critical for replay: when a translation lands and
     * we redraw chat, we must also redraw all other player chat that came in
     * between, otherwise it gets pushed off-screen.
     */
    @Pulse(priority = Event.Priority.HIGH)
    public MessageSendEvent onMessageSendEvent(MessageSendEvent event) {
        if (event.eventMetadata().destination().type() != Destination.Type.CHAT) return event;

        UUID messageUUID = event.eventMetadata().uuid();
        FPlayer receiver = event.receiver();
        String originalText = event.eventMetadata().message();
        if (originalText == null) originalText = "";

        TranslatedMessage translatedMessage = preparedTranslations.getIfPresent(messageUUID);

        translateModule.save(receiver, messageUUID, event.message(), originalText, translatedMessage, true);

        return event;
    }

    /**
     * Server-originated messages (join/quit, vanilla broadcasts, other plugins'
     * messages) arrive here. Save them into the same history so chat redraw
     * preserves them.
     *
     * <p>Self-originated components (those the plugin sent in onMessageSendEvent
     * just above) get filtered out via the isCached flag to avoid duplicates.
     */
    @Pulse(priority = Event.Priority.MONITOR)
    public void onMessageReceiveEvent(MessageReceiveEvent event) {
        if (event.overlay()) return;

        Component component = event.component();

        if (translateModule.isCached(component)) {
            translateModule.removeCache(component);
            return;
        }

        FPlayer receiver = event.player();
        UUID messageUUID = UUID.randomUUID();

        translateModule.save(receiver, messageUUID, component, "", null, false);
    }

    @Pulse(priority = Event.Priority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        translateModule.clearHistory(event.player());
    }
}
