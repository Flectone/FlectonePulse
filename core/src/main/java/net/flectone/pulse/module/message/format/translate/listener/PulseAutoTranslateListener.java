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
        if (!isAutoMode()) return;

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
        if (!isAutoMode()) return event;
        if (event.eventMetadata().destination().type() != Destination.Type.CHAT) return event;

        UUID messageUUID = event.eventMetadata().uuid();
        FPlayer receiver = event.receiver();
        String originalText = event.eventMetadata().message();
        if (originalText == null) originalText = "";

        TranslatedMessage translatedMessage = preparedTranslations.getIfPresent(messageUUID);
        Component originalComponent = event.message();

        // Determine source language. PrepareEvent normally puts a TranslatedMessage
        // into the bridge cache, but dedup-skipped chats (rapid repeats of the
        // same text within 1s) don't go through PrepareEvent — fall back to the
        // sender's own locale so we can still consult the cache.
        String sourceLang = null;
        if (translatedMessage != null) {
            sourceLang = translatedMessage.originalLang();
        } else if (event.eventMetadata().sender() instanceof FPlayer fSender) {
            sourceLang = fSender.getSetting(SettingText.LOCALE);
            if (sourceLang == null) sourceLang = "en_us";
        }

        // Synchronous cache check — applies for both prepared and dedup-skipped
        // sends. The cache is global, so any prior translation of this same text
        // covers this message even though no async fetch was kicked off for THIS
        // uuid. Avoids the race where async cache HIT fires replayForLocale
        // before MessageSendEvent populates history.
        if (receiver != null && !originalText.isEmpty() && sourceLang != null) {
            String receiverLocale = receiver.getSetting(SettingText.LOCALE);
            if (receiverLocale != null && !receiverLocale.equals(sourceLang)) {
                String cached = translateModule.getCachedTranslation(sourceLang, receiverLocale, originalText);
                if (cached != null && !cached.isEmpty() && !cached.equals(originalText)) {
                    String literal = originalText;
                    Component translatedComponent = originalComponent.replaceText(b -> b
                            .matchLiteral(literal)
                            .replacement(cached));
                    if (translatedMessage != null) {
                        translatedMessage.translations().put(receiverLocale, cached);
                    }
                    event = event.withMessage(translatedComponent);
                    fLogger.debug("[AutoTranslate] SendEvent: cache HIT %s→%s for uuid=%s receiver=%s — applied translation directly%s",
                            sourceLang, receiverLocale, messageUUID, receiver.name(),
                            translatedMessage == null ? " (dedup-skipped, history not stored)" : "");
                }
            }
        }

        // Save ORIGINAL component to history only when PrepareEvent prepared a
        // TranslatedMessage. Dedup-skipped messages already got the translation
        // applied to event.message() above; we skip history for them to avoid
        // bloating the global buffer with spam (toggle won't work for those —
        // acceptable trade-off since spam isn't toggled in practice).
        if (translatedMessage != null) {
            translateModule.save(receiver, messageUUID, originalComponent, originalText, translatedMessage, true);
        }

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
        if (!isAutoMode()) return;
        if (event.overlay()) return;

        Component component = event.component();

        if (translateModule.isCached(component)) {
            translateModule.removeCache(component);
            return;
        }

        // Skip components whose plain-text serialization is blank — these are
        // typically chat-pipeline echoes (bubble updates, internal newlines, the
        // plugin's own already-sent chat caught on the return trip) that bloat
        // the global history with empty-text entries. The visible chat doesn't
        // care about them, and replay rendering would show empty lines.
        String plain = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                .serialize(component);
        if (plain.isBlank()) {
            fLogger.debug("[History.receive] skip — empty/blank plain text from MessageReceiveEvent for receiver=%s",
                    event.player() == null ? "null" : event.player().name());
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

    /** True when message.format.translate.auto is enabled (default). */
    private boolean isAutoMode() {
        return !Boolean.FALSE.equals(translateModule.config().auto());
    }
}
