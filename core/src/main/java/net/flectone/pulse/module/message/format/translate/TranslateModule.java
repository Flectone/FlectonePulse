package net.flectone.pulse.module.message.format.translate;

import com.google.common.cache.Cache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.ModuleLocalization;
import net.flectone.pulse.module.message.format.translate.listener.PulseAutoTranslateListener;
import net.flectone.pulse.module.message.format.translate.listener.PulseTranslateListener;
import net.flectone.pulse.module.message.format.translate.model.TranslateHistoryMessage;
import net.flectone.pulse.module.message.format.translate.model.TranslatedMessage;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.sender.MessageSender;
import net.flectone.pulse.processing.parser.string.UUIDParser;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.service.TranslationCacheService;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TranslateModule implements ModuleLocalization<Localization.Message.Format.Translate> {

    // Server-wide, time-ordered chat history. Each entry is shared across all its
    // viewers (Set<UUID>) — one Component per message, not one per receiver.
    // Synchronize on this list; snapshots take a local copy under lock.
    private final List<TranslateHistoryMessage> globalHistory = new ArrayList<>();

    // Per-player "show original" toggles, kept separate because history entries are shared.
    private final Map<UUID, Set<UUID>> playerOriginalToggles = new ConcurrentHashMap<>();

    // Components the plugin itself sent — dedup against MessageReceiveEvent.
    private final List<Component> selfOriginatedComponents = new CopyOnWriteArrayList<>();

    private final @Named("translateMessage") Cache<String, UUID> messageCache;
    private final FileFacade fileFacade;
    private final ListenerRegistry listenerRegistry;
    private final MessagePipeline messagePipeline;
    private final ModuleController moduleController;
    private final UUIDParser uuidParser;
    private final FPlayerService fPlayerService;
    private final MessageSender messageSender;
    private final TranslationCacheService translationCacheService;
    private final FLogger fLogger;
    private final SocialService socialService;

    @Override
    public void onEnable() {
        listenerRegistry.register(PulseTranslateListener.class);
        listenerRegistry.register(PulseAutoTranslateListener.class);
    }

    @Override
    public void onDisable() {
        messageCache.invalidateAll();
        synchronized (globalHistory) {
            globalHistory.clear();
        }
        playerOriginalToggles.clear();
        selfOriginatedComponents.clear();
        translationCacheService.shutdown();
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_FORMAT_TRANSLATE;
    }

    @Override
    public Message.Format.Translate config() {
        return fileFacade.message().format().translate();
    }

    @Override
    public Permission.Message.Format.Translate permission() {
        return fileFacade.permission().message().format().translate();
    }

    @Override
    public Localization.Message.Format.Translate localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).message().format().translate();
    }

    public UUID saveMessage(String message) {
        UUID uuid = messageCache.getIfPresent(message);
        if (uuid == null) {
            uuid = UUID.randomUUID();
            messageCache.put(message, uuid);
        }

        return uuid;
    }

    // Adds the <translation> tag resolver. auto:true (default) builds a /toggleoriginal
    // button; auto:false keeps the classic on-demand /translateto path.
    public MessageContext addTag(MessageContext messageContext) {
        FEntity sender = messageContext.sender();
        if (moduleController.isDisabledFor(this, sender)) return messageContext;

        FPlayer receiver = messageContext.receiver();
        // Auto-mode (toggle button) applies when the master auto-translate toggle is on (default true).
        boolean autoMode = !Boolean.FALSE.equals(config().auto());

        return messageContext.addTagResolver(messagePipeline.resolver(MessagePipeline.ReplacementTag.TRANSLATION.getTagName(), (argumentQueue, _) -> {
            String senderLocale = sender instanceof FPlayer fSender ? socialService.getSetting(fSender, SettingText.LOCALE) : null;
            String receiverLocale = receiver != null ? socialService.getSetting(receiver, SettingText.LOCALE) : null;

            UUID messageUUID = messageContext.messageUUID();
            String action;

            if (autoMode) {
                // Auto-translate mode — only player-to-player messages get the button
                if (!(sender instanceof FPlayer)) {
                    return Tag.selfClosingInserting(Component.empty());
                }
                // Same-locale receiver — no translation done, no button
                if (senderLocale != null && senderLocale.equals(receiverLocale)) {
                    return Tag.selfClosingInserting(Component.empty());
                }
                action = localization(receiver).action();
                action = Strings.CS.replace(action, "<message>", messageUUID.toString());
            } else {
                // Classic /translateto mode — author's original behavior.
                // Parse optional <translation:src:dst> args, fallback to auto/receiver-locale.
                String firstLang = "auto";
                String secondLang = receiverLocale;
                if (argumentQueue.hasNext()) {
                    Tag.Argument first = argumentQueue.pop();
                    if (argumentQueue.hasNext()) {
                        Tag.Argument second = argumentQueue.pop();
                        if (argumentQueue.hasNext()) {
                            firstLang = first.value();
                            secondLang = second.value();
                        } else {
                            secondLang = first.value();
                        }
                    }
                }
                action = localization(receiver).actionManual();
                if (action == null || action.isEmpty()) {
                    // Fallback to auto-mode template if user didn't set actionManual.
                    action = localization(receiver).action();
                }
                action = Strings.CS.replaceOnce(action, "<language>", firstLang);
                action = Strings.CS.replaceOnce(action, "<language>", secondLang == null ? "ru_ru" : secondLang);
                action = Strings.CS.replace(action, "<message>", saveMessage(messageContext.userMessage()).toString());
            }

            return Tag.selfClosingInserting(messagePipeline.build(MessageContext.builder()
                    .sender(sender)
                    .receiver(receiver)
                    .message(action)
                    .flags(messageContext.flags())
                    .flags(
                            new MessageFlag[]{MessageFlag.MENTION_MODULE, MessageFlag.INTERACTIVE_CHAT_COMPAT, MessageFlag.QUESTIONANSWER_MODULE, MessageFlag.TRANSLATE_MODULE, MessageFlag.PLAYER_MESSAGE},
                            new boolean[]{false, false, false, false, false}
                    )
                    .build()
            ));
        }));
    }

    // Kicks off async translation for each unique online locale != source. The returned
    // TranslatedMessage's translations map is filled by the callbacks. Null if only the
    // source locale is online (nothing to translate).
    public @Nullable TranslatedMessage translateToAllLocales(String originalText, String sourceLang) {
        if (moduleController.isDisabledFor(this, FPlayer.UNKNOWN)) return null;

        Set<String> uniqueLocales = fPlayerService.getOnlineFPlayers().stream()
                .map(player -> socialService.getSetting(player, SettingText.LOCALE))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        uniqueLocales.add(sourceLang);

        if (uniqueLocales.size() <= 1) {
            return null;
        }

        Map<String, String> translations = new ConcurrentHashMap<>();
        translations.put(sourceLang, originalText);

        TranslatedMessage translatedMessage = TranslatedMessage.builder()
                .originalText(originalText)
                .originalLang(sourceLang)
                .translations(translations)
                .build();

        List<String> targetLangs = uniqueLocales.stream()
                .filter(targetLang -> !targetLang.equals(sourceLang))
                .toList();

        List<String> providers = config().providers();

        targetLangs.forEach(targetLang -> {
            // SYNC cache hit → populate translations immediately, NO replay needed.
            // The receiver's MessageSendEvent will pick up the cached value via its
            // own sync check and apply replaceText to the outgoing Component.
            // Triggering replay here would race against SendEvent and re-render
            // stale history, causing visible "ghost" duplicates of older messages.
            String cached = translationCacheService.get(sourceLang, targetLang, originalText);
            if (cached != null && !cached.isEmpty() && !cached.equals(originalText)) {
                translations.put(targetLang, cached);
                return;
            }

            // Cache miss — kick off async chain. On success, fire replay so older
            // history entries (which were originally sent before this translation
            // existed) update to the translated version.
            translationCacheService.translateAsync(sourceLang, targetLang, originalText, providers)
                    .thenAccept(translated -> {
                        String text = (translated != null && !translated.isEmpty()) ? translated : originalText;
                        translations.put(targetLang, text);
                        if (!text.equals(originalText)) {
                            replayForLocale(targetLang);
                        }
                    })
                    .exceptionally(throwable -> {
                        fLogger.warning(throwable, "[AutoTranslate] chain failed %s, falling back to original",
                                sourceLang + "→" + targetLang);
                        translations.put(targetLang, originalText);
                        return null;
                    });
        });

        return translatedMessage;
    }

    public @Nullable String getCachedTranslation(String sourceLang, String targetLang, String text) {
        return translationCacheService.get(sourceLang, targetLang, text);
    }

    // Ensures a translation for a single (sourceLang -> targetLang) pair is available.
    // Used by private-message (tell/reply) receiver copies: unlike broadcast chat, the
    // receiver copy isn't guaranteed to have its locale covered by translateToAllLocales
    // (the sender copy is deduped against it), so it drives its own async fill here.
    // On completion replay redraws history for the given receiver only — keeping private
    // messages scoped to their single viewer (see sendUpdate's viewers() filter).
    public void ensureTranslationForReceiver(String sourceLang, String targetLang, String text, UUID receiverUUID, UUID messageUUID) {
        if (moduleController.isDisabledFor(this, FPlayer.UNKNOWN)) {
            return;
        }
        if (sourceLang == null || targetLang == null || text == null || text.isEmpty()) {
            return;
        }
        if (sourceLang.equals(targetLang)) {
            return;
        }

        String cached = translationCacheService.get(sourceLang, targetLang, text);
        if (cached != null && !cached.isEmpty() && !cached.equals(text)) {
            // Already cached — write it into the receiver's history entry, then redraw exactly
            // like chat (sendUpdate). The translation is now present in translations BEFORE the
            // repaint, so getDisplayComponent renders the translated line. Order is strict:
            // applyTranslationToEntry first, sendUpdate after.
            applyTranslationToEntry(messageUUID, targetLang, cached);
            sendUpdate(receiverUUID);
            return;
        }

        translationCacheService.translateAsync(sourceLang, targetLang, text, config().providers())
                .thenAccept(translated -> {
                    if (translated == null || translated.isEmpty() || translated.equals(text)) {
                        return;
                    }
                    // Write the translation straight into the matching history entry FIRST, so
                    // the value is present in translations before the repaint, then redraw the
                    // receiver's history exactly like chat does (sendUpdate). Because sendUpdate
                    // filters by viewers.contains(receiverUUID), only this receiver's own history
                    // is repainted — no leak to other same-locale players. Order is strict:
                    // applyTranslationToEntry first, sendUpdate after.
                    applyTranslationToEntry(messageUUID, targetLang, translated);
                    sendUpdate(receiverUUID);
                })
                .exceptionally(throwable -> {
                    fLogger.warning(throwable, "[AutoTranslate] private-message chain failed %s",
                            sourceLang + "→" + targetLang);
                    return null;
                });
    }

    // Writes a translation directly into the TranslatedMessage of the matching history entry.
    // Used by the private-message cold path so replay shows the translation deterministically,
    // independent of when the global translation cache becomes readable.
    private void applyTranslationToEntry(UUID messageUUID, String targetLang, String translation) {
        if (messageUUID == null || targetLang == null || translation == null || translation.isEmpty()) return;
        synchronized (globalHistory) {
            for (TranslateHistoryMessage e : globalHistory) {
                if (e.uuid().equals(messageUUID)) {
                    TranslatedMessage tm = e.translatedMessage();
                    if (tm != null) {
                        tm.translations().put(targetLang, translation);
                    }
                    return;
                }
            }
        }
    }

    // After a translation lands, redraw chat for every online receiver with that locale.
    private void replayForLocale(String locale) {
        if (locale == null) return;

        fPlayerService.getOnlineFPlayers().stream()
                .filter(player -> locale.equals(socialService.getSetting(player, SettingText.LOCALE)))
                .forEach(player -> sendUpdate(player.uuid()));
    }

    // Stores the chat event in the global history and adds this receiver to the entry's
    // viewer set. If the UUID already exists, only the viewer is added — Component is shared.
    public void save(FPlayer receiver,
                     UUID messageUUID,
                     Component component,
                     String originalText,
                     @Nullable TranslatedMessage translatedMessage,
                     boolean needToCache) {
        if (receiver.isUnknown()) {
            return;
        }
        if (!receiver.isOnline()) {
            return;
        }

        UUID receiverUUID = receiver.uuid();
        String receiverLocale = socialService.getSetting(receiver, SettingText.LOCALE);

        synchronized (globalHistory) {
            TranslateHistoryMessage existing = null;
            for (TranslateHistoryMessage e : globalHistory) {
                if (e.uuid().equals(messageUUID)) {
                    existing = e;
                    break;
                }
            }

            if (existing != null) {
                existing.viewers().add(receiverUUID);
                if (receiverLocale != null && !existing.componentsByLocale().containsKey(receiverLocale)) {
                    existing.componentsByLocale().put(receiverLocale, component);
                }
                // save() runs once per receiver; the first call may carry a null TM (e.g.
                // the sender, whose locale matches the source). Merge in a later non-null TM
                // so the entry stays toggleable instead of stuck on the first save's null.
                if (translatedMessage != null) {
                    if (existing.translatedMessage() == null) {
                        // Record fields are final — replace the entry in-place.
                        TranslateHistoryMessage updated = existing.withTranslatedMessage(translatedMessage);
                        int idx = globalHistory.indexOf(existing);
                        if (idx >= 0) globalHistory.set(idx, updated);
                        existing = updated;
                    } else {
                        existing.translatedMessage().translations().putAll(translatedMessage.translations());
                    }
                }
            } else {
                TranslateHistoryMessage entry = TranslateHistoryMessage.create(
                        messageUUID, receiverLocale, component, originalText, translatedMessage
                );
                entry.viewers().add(receiverUUID);
                globalHistory.add(entry);
                while (globalHistory.size() > historyLength()) {
                    globalHistory.remove(0);
                }
            }
        }

        if (needToCache && !isCached(component)) {
            selfOriginatedComponents.add(component);
        }
    }

    public boolean isCached(Component component) {
        return selfOriginatedComponents.contains(component);
    }

    public void removeCache(Component component) {
        selfOriginatedComponents.remove(component);
    }

    // Registers a Component as plugin-originated for ReceiveEvent dedup. Must be the
    // EXACT instance carried by the outgoing packet (post-withMessage) — isCached compares
    // by reference, so the wrong variant silently breaks dedup and duplicates the entry.
    public void markSelfOriginated(Component sentComponent) {
        if (sentComponent == null) return;
        if (!selfOriginatedComponents.contains(sentComponent)) {
            selfOriginatedComponents.add(sentComponent);
        }
    }

    // Player left — drop them from every viewer set and discard their toggles.
    // Entries themselves stay (other players may still view them).
    public void clearHistory(FPlayer fPlayer) {
        UUID playerUUID = fPlayer.uuid();
        synchronized (globalHistory) {
            for (TranslateHistoryMessage entry : globalHistory) {
                entry.viewers().remove(playerUUID);
            }
        }
        playerOriginalToggles.remove(playerUUID);
    }

    // Brute-force chat redraw for one receiver: filter history to what they saw, fill
    // missing translations from cache, then push empty newlines to scroll old chat
    // off-screen before reprinting.
    public void sendUpdate(UUID receiverUUID) {
        FPlayer fPlayer = fPlayerService.getFPlayer(receiverUUID);
        String playerLocale = socialService.getSetting(fPlayer, SettingText.LOCALE);

        List<TranslateHistoryMessage> visible;
        synchronized (globalHistory) {
            visible = globalHistory.stream()
                    .filter(e -> e.viewers().contains(receiverUUID))
                    .toList();
        }

        if (visible.isEmpty()) {
            return;
        }

        fillTranslationsFromCache(visible, playerLocale);

        Set<UUID> toggles = playerOriginalToggles.getOrDefault(receiverUUID, Set.of());

        int len = historyLength();
        int emptyLines = Math.max(0, len - visible.size());

        for (int i = 0; i < emptyLines; i++) {
            messageSender.sendMessage(fPlayer, Component.newline(), true);
        }
        for (TranslateHistoryMessage entry : visible) {
            boolean showOriginal = toggles.contains(entry.uuid());
            messageSender.sendMessage(fPlayer, entry.getDisplayComponent(playerLocale, showOriginal), true);
        }
    }

    private void fillTranslationsFromCache(List<TranslateHistoryMessage> entries, String receiverLocale) {
        if (receiverLocale == null) return;
        for (TranslateHistoryMessage entry : entries) {
            TranslatedMessage tm = entry.translatedMessage();
            if (tm == null) continue;
            if (receiverLocale.equals(tm.originalLang())) continue;
            if (tm.hasTranslation(receiverLocale)) {
                String existing = tm.getTranslation(receiverLocale);
                if (existing != null && !existing.isEmpty() && !existing.equals(entry.originalText())) continue;
            }
            String cached = translationCacheService.get(tm.originalLang(), receiverLocale, entry.originalText());
            if (cached != null && !cached.isEmpty() && !cached.equals(entry.originalText())) {
                tm.translations().put(receiverLocale, cached);
            }
        }
    }

    public boolean toggleOriginal(FPlayer fPlayer, UUID messageUUID) {
        if (moduleController.isDisabledFor(this, fPlayer)) {
            return false;
        }
        if (messageUUID == null) {
            return false;
        }

        UUID playerUUID = fPlayer.uuid();

        TranslateHistoryMessage entry = null;
        synchronized (globalHistory) {
            for (TranslateHistoryMessage e : globalHistory) {
                if (e.uuid().equals(messageUUID)) {
                    entry = e;
                    break;
                }
            }
        }

        if (entry == null) {
            return false;
        }
        if (!entry.viewers().contains(playerUUID)) {
            return false;
        }
        if (!entry.hasTranslations()) {
            return false;
        }

        Set<UUID> toggles = playerOriginalToggles.computeIfAbsent(playerUUID, _ -> ConcurrentHashMap.newKeySet());
        boolean wasShowingOriginal = toggles.contains(messageUUID);
        boolean nowShowingOriginal = !wasShowingOriginal;
        if (nowShowingOriginal) toggles.add(messageUUID);
        else toggles.remove(messageUUID);

        // If toggled to "show translation" but no usable translation exists, kick off retry.
        if (!nowShowingOriginal) {
            maybeRetryTranslation(fPlayer, entry);
        }
        sendUpdate(playerUUID);
        return true;
    }

    private void maybeRetryTranslation(FPlayer fPlayer, TranslateHistoryMessage entry) {
        TranslatedMessage tm = entry.translatedMessage();
        if (tm == null) return;

        String playerLocale = socialService.getSetting(fPlayer, SettingText.LOCALE);
        if (playerLocale == null) return;
        if (playerLocale.equals(tm.originalLang())) return;

        String existing = tm.getTranslation(playerLocale);
        boolean usable = existing != null && !existing.isEmpty() && !existing.equals(entry.originalText());
        if (usable) return;

        UUID receiverUUID = fPlayer.uuid();
        translationCacheService.translateAsync(tm.originalLang(), playerLocale, entry.originalText(), config().providers())
                .thenAccept(result -> {
                    if (result == null || result.isEmpty() || result.equals(entry.originalText())) {
                        return;
                    }
                    tm.translations().put(playerLocale, result);
                    sendUpdate(receiverUUID);
                });
    }

    // Borrows DeleteModule's config so both push out the same number of empty lines.
    private int historyLength() {
        return fileFacade.message().format().moderation().delete().historyLength();
    }

    @Nullable
    public String getMessage(String stringUUID) {
        UUID uuid = uuidParser.parse(stringUUID);
        return uuid != null ? getMessage(uuid) : null;
    }

    public @Nullable String getMessage(UUID uuid) {
        return messageCache.asMap().entrySet()
                .stream()
                .filter(entry -> entry.getValue().equals(uuid))
                .findAny()
                .map(Map.Entry::getKey)
                .orElse(null);
    }
}
