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
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.constant.SettingText;
import net.kyori.adventure.text.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

// Drives auto-translation and captures the full chat stream (chat, announcements,
// join/quit) so the brute-redraw can restore everything a player saw — otherwise
// replay would only redraw player chat and push the rest off-screen.
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PulseAutoTranslateListener implements PulseListener {

    private final TranslateModule translateModule;
    private final SocialService socialService;

    // Bridges per-message PrepareEvent to per-receiver SendEvent.
    private final Cache<UUID, TranslatedMessage> preparedTranslations = CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build();

    // Dedupes duplicate PrepareEvent fires within 1s for the same (sender, text):
    // Paper/Purpur sometimes dispatch chat twice, which would double API calls/entries/replays.
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

        String senderLocale = socialService.getSetting(sender, SettingText.LOCALE);
        if (senderLocale == null) senderLocale = "en_us";

        String dedupKey = sender.uuid() + ":" + senderLocale + ":" + message;
        if (recentMessages.getIfPresent(dedupKey) != null) {
            return;
        }
        recentMessages.put(dedupKey, Boolean.TRUE);

        TranslatedMessage translatedMessage = translateModule.translateToAllLocales(message, senderLocale);
        if (translatedMessage == null) {
            return;
        }

        preparedTranslations.put(messageUUID, translatedMessage);
    }

    // Records EVERY chat into history (translated or not). Needed for replay: when a
    // translation lands we redraw all chat that came between, else it scrolls off-screen.
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
            sourceLang = socialService.getSetting(fSender, SettingText.LOCALE);
            if (sourceLang == null) sourceLang = "en_us";
        }

        // Synchronous cache check — applies for both prepared and dedup-skipped
        // sends. If the cache already has a translation for this receiver's
        // locale, apply it inline so the chat is translated on first display.
        if (receiver != null && !originalText.isEmpty() && sourceLang != null) {
            String receiverLocale = socialService.getSetting(receiver, SettingText.LOCALE);
            if (receiverLocale != null && !receiverLocale.equals(sourceLang)) {
                String cached = translateModule.getCachedTranslation(sourceLang, receiverLocale, originalText);
                if (cached != null && !cached.isEmpty() && !cached.equals(originalText)) {
                    String literal = originalText;
                    Component translatedComponent = originalComponent.replaceText(b -> b
                            .matchLiteral(literal)
                            .replacement(cached));

                    // For dedup-skipped chats PrepareEvent didn't create a TranslatedMessage.
                    // Synthesize one so the entry that's about to land in history carries the
                    // translation data — toggle works for these too, history isn't a black hole.
                    if (translatedMessage == null) {
                        Map<String, String> translations = new ConcurrentHashMap<>();
                        translations.put(sourceLang, originalText);
                        translations.put(receiverLocale, cached);
                        translatedMessage = TranslatedMessage.builder()
                                .originalText(originalText)
                                .originalLang(sourceLang)
                                .translations(translations)
                                .build();
                    } else {
                        translatedMessage.translations().put(receiverLocale, cached);
                    }

                    event = event.withMessage(translatedComponent);
                }
            }
        }

        // Always save. translatedMessage may be null (system/server messages) — those stay
        // non-toggleable but are still redrawn so history is intact. needToCache=false: dedup
        // is handled below via markSelfOriginated against the actual outgoing component.
        translateModule.save(receiver, messageUUID, originalComponent, originalText, translatedMessage, false);

        // Register the component actually about to be sent (may be the translated variant).
        // ReceiveEvent will see the same reference and isCached skips it — no duplicate entry.
        translateModule.markSelfOriginated(event.message());

        return event;
    }

    // Server-originated messages (join/quit, broadcasts, other plugins) land here and go
    // into the same history. Self-originated components are filtered via isCached.
    @Pulse(priority = Event.Priority.MONITOR)
    public void onMessageReceiveEvent(MessageReceiveEvent event) {
        if (!isAutoMode()) return;
        if (event.overlay()) return;

        Component component = event.component();

        if (translateModule.isCached(component)) {
            translateModule.removeCache(component);
            return;
        }

        // Skip blank components — chat-pipeline echoes (bubble updates, internal newlines)
        // that would bloat history with empty entries and render as blank replay lines.
        String plain = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                .serialize(component);
        if (plain.isBlank()) {
            return;
        }

        FPlayer receiver = event.player();
        UUID messageUUID = UUID.randomUUID();

        translateModule.save(receiver, messageUUID, component, "", null, false);
    }

    @Pulse(priority = Event.Priority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Default true — a reconnect should start with a clean chat history.
        // Set message.format.translate.clear_history_on_quit: false to preserve
        // entries across reconnects (useful for short reloads).
        if (!Boolean.FALSE.equals(translateModule.config().clearHistoryOnQuit())) {
            translateModule.clearHistory(event.player());
        }
    }

    private boolean isAutoMode() {
        return !Boolean.FALSE.equals(translateModule.config().auto());
    }
}
