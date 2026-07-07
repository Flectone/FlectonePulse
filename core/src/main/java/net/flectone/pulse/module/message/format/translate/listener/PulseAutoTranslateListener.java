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
import net.flectone.pulse.module.message.format.translate.model.TranslateHistoryMessage;
import net.flectone.pulse.module.message.format.translate.model.TranslatedMessage;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import net.kyori.adventure.text.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
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

    // Coalesces the burst of identical external-broadcast packets (join/quit, other plugins)
    // into ONE shared history entry. A single server broadcast is dispatched as a separate
    // per-receiver packet, so without this every same-locale receiver would mint a fresh
    // random UUID and add its OWN history entry — one join on 50 players = 50 entries, which
    // evicts translatable chat out of history before a player can toggle it. Keyed by
    // structural component equality so same-locale receivers map to the same UUID and share
    // one entry via the viewers set (like self-originated messages already do). The 1s window
    // only merges the near-simultaneous burst of a single broadcast, not distinct later
    // messages that happen to carry the same text.
    private final Cache<Component, UUID> externalMessageUUIDs = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.SECONDS)
            .build();

    @Pulse(priority = Event.Priority.HIGH)
    public void onMessagePrepareEvent(MessagePrepareEvent event) {
        if (!isChatAuto()) return;

        // Only PUBLIC chat may be pre-translated to every online locale. Private messages
        // (tell/reply) also carry destination CHAT and would otherwise reach this handler via
        // their sender copy, leaking the private text to external translators for EVERY online
        // locale — including players who will never see the PM — and burning API quota. Gate on
        // the source module, same as onMessageSendEvent (moduleName == MESSAGE_CHAT). The PM
        // receiver still gets translated: its receiver copy is handled per-recipient in
        // onMessageSendEvent via ensureTranslationForReceiver (its own async fill, scoped to the
        // real addressee), which does not depend on this all-locales pre-warm.
        if (event.moduleName() != ModuleName.MESSAGE_CHAT) return;

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
        if (!isAutoMode()) {
            return event;
        }
        if (event.eventMetadata().destination().type() != Destination.Type.CHAT) {
            return event;
        }

        UUID messageUUID = event.eventMetadata().uuid();
        FPlayer receiver = event.receiver();
        String originalText = event.eventMetadata().message();
        if (originalText == null) originalText = "";

        // Cold private-message path: the receiver fill must run AFTER save() so the history
        // entry (with the synthesized TM) already exists when ensureTranslationForReceiver's
        // synchronous cache-HIT branch calls applyTranslationToEntry. We capture the call here
        // and fire it once save() has landed the entry into globalHistory.
        Runnable deferredReceiverFill = null;

        // Decide whether THIS message may be translated, strictly by its source module:
        // broadcast chat -> auto.chat, private messages (tell/reply) -> auto.private.
        // System/server messages (join/quit, other modules) are never translated here —
        // they're only captured for history/replay below (gated by isAutoMode()).
        boolean broadcastChat = event.moduleName() == ModuleName.MESSAGE_CHAT;
        boolean privateMessage = event.moduleName() == ModuleName.COMMAND_TELL
                || event.moduleName() == ModuleName.COMMAND_REPLY;
        boolean translateChat = broadcastChat && isChatAuto();
        boolean translatePrivate = privateMessage && isPrivateAuto();

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
        // Gated per message type: broadcast chat only when auto.chat, private
        // messages only when auto.private — never cross-influences the other.
        if ((translateChat || translatePrivate)
                && receiver != null && !originalText.isEmpty() && sourceLang != null) {
            String receiverLocale = socialService.getSetting(receiver, SettingText.LOCALE);
            if (receiverLocale != null && !receiverLocale.equals(sourceLang)) {
                String cached = translateModule.getCachedTranslation(sourceLang, receiverLocale, originalText);
                if (cached != null && !cached.isEmpty() && !cached.equals(originalText)) {
                    // Use the shared cross-node replacer (same as getDisplayComponent's cold path):
                    // matchLiteral fails when format modules split the message across styled nodes,
                    // which is exactly what happens for private messages.
                    Component translatedComponent =
                            TranslateHistoryMessage.replaceMessageText(originalComponent, originalText, cached);

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
                } else if (translatedMessage == null && (translatePrivate || translateChat)) {
                    // Cold cache AND no prepared TranslatedMessage. Two shapes reach here:
                    //  1. the private-message (tell/reply) receiver copy — its PrepareEvent is
                    //     deduped against the sender copy (same sender+text), so no TM was ever
                    //     wired to its UUID;
                    //  2. a rapid CHAT repeat — the same text sent twice within 1s. The second
                    //     PrepareEvent is swallowed by recentMessages dedup, so translateToAllLocales
                    //     never ran for this UUID and preparedTranslations has nothing. On a warm
                    //     cache the sync branch above already handles it; on a COLD cache the second
                    //     entry would land with tm == null and stay untranslated FOREVER —
                    //     fillTranslationsFromCache skips tm == null, so even once the first copy's
                    //     translation lands in the shared cache the second entry has no vessel to
                    //     receive it and no button ever works.
                    // Either way, without a TM the history entry can never be backfilled by replay.
                    // Synthesize a TM now so fillTranslationsFromCache can fill it later, and drive
                    // the async fill ourselves scoped to THIS receiver (no leak to other same-locale
                    // players). For the chat repeat this issues NO extra HTTP: ensureTranslationFor
                    // Receiver → translateAsync reuses the first copy's in-flight future (same
                    // source:target:text key) or the now-warm cache, so the dedup's real purpose —
                    // not double-calling the external API — is preserved. It also closes the race
                    // where the first copy's translation lands between this cold cache check and
                    // save(): the cache re-check / shared future inside ensureTranslationForReceiver
                    // still delivers it to this entry.
                    Map<String, String> translations = new ConcurrentHashMap<>();
                    translations.put(sourceLang, originalText);
                    translatedMessage = TranslatedMessage.builder()
                            .originalText(originalText)
                            .originalLang(sourceLang)
                            .translations(translations)
                            .build();

                    // Defer the actual fill until AFTER save() (below) so the history entry —
                    // carrying THIS synthesized TM — already exists when the cache-HIT branch
                    // synchronously calls applyTranslationToEntry → sendUpdate.
                    final String fSourceLang = sourceLang;
                    final String fReceiverLocale = receiverLocale;
                    final String fOriginalText = originalText;
                    final UUID fReceiverUUID = receiver.uuid();
                    final UUID fMessageUUID = messageUUID;
                    deferredReceiverFill = () -> translateModule.ensureTranslationForReceiver(
                            fSourceLang, fReceiverLocale, fOriginalText, fReceiverUUID, fMessageUUID);
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

        // Cold private-message receiver fill, now that the history entry (with its synthesized
        // TM) is in place: ensureTranslationForReceiver can apply the translation to the entry
        // and then sendUpdate repaints exactly the receiver's own history.
        if (deferredReceiverFill != null) {
            deferredReceiverFill.run();
        }

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

        // Share one UUID (hence one history entry + shared viewers) across all receivers of the
        // same external broadcast. Component.get(key, loader) is atomic, so a burst of concurrent
        // per-receiver packets on netty threads resolves to a single UUID minted exactly once.
        UUID messageUUID;
        try {
            messageUUID = externalMessageUUIDs.get(component, UUID::randomUUID);
        } catch (ExecutionException e) {
            messageUUID = UUID.randomUUID();
        }

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

    // Any auto path on (chat OR private). Used by the shared history-capture/replay
    // logic that serves both broadcast chat and private messages.
    private boolean isAutoMode() {
        return isChatAuto() || isPrivateAuto();
    }

    // message.format.translate.auto — single master toggle for chat AND private messages.
    private boolean isChatAuto() {
        return !Boolean.FALSE.equals(translateModule.config().auto());
    }

    // message.format.translate.auto — same master toggle drives private messages (tell/reply).
    private boolean isPrivateAuto() {
        return !Boolean.FALSE.equals(translateModule.config().auto());
    }
}
