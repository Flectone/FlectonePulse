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

    /**
     * Global server-wide chat history, time-ordered. Each entry carries the
     * formatted Component and a {@code Set<UUID> viewers} — every player who
     * saw it. Memory: 100 players seeing one message = 1 entry with a 100-set
     * of viewer UUIDs, not 100 duplicated Component trees.
     *
     * <p>Synchronize on this list for add/remove operations; iteration during
     * a snapshot (sendUpdate, toggle lookup) takes a local copy under lock.
     */
    private final List<TranslateHistoryMessage> globalHistory = new java.util.ArrayList<>();

    /**
     * Per-player toggle state: which message UUIDs that player has flipped to
     * "show original". Stored separately from the history entries because it's
     * per-player; the entries themselves are shared.
     */
    private final Map<UUID, Set<UUID>> playerOriginalToggles = new ConcurrentHashMap<>();

    /** Components originated by FlectonePulse itself — dedup against MessageReceiveEvent. */
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

    /**
     * Adds the {@code <translation>} tag resolver. Two button modes:
     *
     * <ul>
     *   <li><b>auto: true</b> (default) — our async-translate path. Button runs
     *       {@code /toggleoriginal <uuid>}. Hidden for same-locale receivers
     *       (no translation done for them so nothing to toggle).</li>
     *   <li><b>auto: false</b> — author's classic on-demand path. Button runs
     *       {@code /translateto <src> <dst> <uuid>} which synchronously
     *       translates and dispatches a new chat line. No history, no toggle.</li>
     * </ul>
     */
    public MessageContext addTag(MessageContext messageContext) {
        FEntity sender = messageContext.sender();
        if (moduleController.isDisabledFor(this, sender)) return messageContext;

        FPlayer receiver = messageContext.receiver();
        boolean autoMode = !Boolean.FALSE.equals(config().auto()); // default true

        return messageContext.addTagResolver(messagePipeline.resolver(MessagePipeline.ReplacementTag.TRANSLATION.getTagName(), (argumentQueue, _) -> {
            String senderLocale = sender instanceof FPlayer fSender ? socialService.getSetting(fSender, SettingText.LOCALE) : null;
            String receiverLocale = receiver != null ? socialService.getSetting(receiver, SettingText.LOCALE) : null;

            UUID messageUUID = messageContext.messageUUID();
            String action;

            if (autoMode) {
                // Auto-translate mode — only player-to-player messages get the button
                if (!(sender instanceof FPlayer)) {
                    fLogger.debug("[History.addTag] no button — sender is not FPlayer (uuid=%s)", messageUUID);
                    return Tag.selfClosingInserting(Component.empty());
                }
                // Same-locale receiver — no translation done, no button
                if (senderLocale != null && senderLocale.equals(receiverLocale)) {
                    fLogger.debug("[History.addTag] no button — same locale %s for sender=%s receiver=%s uuid=%s",
                            senderLocale,
                            sender instanceof FPlayer fp ? fp.name() : "?",
                            receiver == null ? "null" : receiver.name(),
                            messageUUID);
                    return Tag.selfClosingInserting(Component.empty());
                }
                action = localization(receiver).action();
                action = Strings.CS.replace(action, "<message>", messageUUID.toString());
                fLogger.debug("[History.addTag] auto button: receiver=%s receiverLocale=%s senderLocale=%s uuid=%s → /toggleoriginal %s",
                        receiver == null ? "null" : receiver.name(),
                        receiverLocale, senderLocale, messageUUID, messageUUID);
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

    /**
     * Start async translation jobs for each unique online locale other than the source.
     * Returns the {@link TranslatedMessage} immediately — its {@code translations} map
     * is populated by the async callbacks. When each translation lands, the listener
     * triggers a chat redraw via {@link #sendUpdate(UUID)} for matching receivers.
     *
     * <p>Returns {@code null} if only one locale is online (source locale only) — no
     * translation is needed.
     */
    public @Nullable TranslatedMessage translateToAllLocales(String originalText, String sourceLang) {
        if (moduleController.isDisabledFor(this, FPlayer.UNKNOWN)) return null;

        Set<String> uniqueLocales = fPlayerService.getOnlineFPlayers().stream()
                .map(player -> socialService.getSetting(player, SettingText.LOCALE))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        uniqueLocales.add(sourceLang);

        fLogger.debug("[AutoTranslate] translateToAllLocales: source=%s text='%s' unique locales=%s",
                sourceLang, originalText, uniqueLocales);

        if (uniqueLocales.size() <= 1) {
            fLogger.debug("[AutoTranslate] translateToAllLocales: skip — only one locale online");
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

        fLogger.debug("[AutoTranslate] translateToAllLocales: launching %d async translation(s) %s → %s",
                targetLangs.size(), sourceLang, targetLangs);

        java.util.List<String> providers = config().providers();
        fLogger.debug("[AutoTranslate] translateToAllLocales: provider chain=%s", providers);

        targetLangs.forEach(targetLang -> {
            // SYNC cache hit → populate translations immediately, NO replay needed.
            // The receiver's MessageSendEvent will pick up the cached value via its
            // own sync check and apply replaceText to the outgoing Component.
            // Triggering replay here would race against SendEvent and re-render
            // stale history, causing visible "ghost" duplicates of older messages.
            String cached = translationCacheService.get(sourceLang, targetLang, originalText);
            if (cached != null && !cached.isEmpty() && !cached.equals(originalText)) {
                translations.put(targetLang, cached);
                fLogger.debug("[AutoTranslate] translateToAllLocales: %s→%s cache HIT='%s' — populated translations, no async, no replay",
                        sourceLang, targetLang, cached);
                return;
            }

            // Cache miss — kick off async chain. On success, fire replay so older
            // history entries (which were originally sent before this translation
            // existed) update to the translated version.
            translationCacheService.translateAsync(sourceLang, targetLang, originalText, providers)
                    .thenAccept(translated -> {
                        String text = (translated != null && !translated.isEmpty()) ? translated : originalText;
                        translations.put(targetLang, text);
                        if (text.equals(originalText)) {
                            fLogger.debug("[AutoTranslate] translation %s→%s == original (or no provider succeeded), skipping replay",
                                    sourceLang, targetLang);
                        } else {
                            fLogger.debug("[AutoTranslate] translation arrived %s→%s, triggering replay for matching receivers",
                                    sourceLang, targetLang);
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

    /** Synchronous cache lookup — returns the cached translation if present. */
    public @Nullable String getCachedTranslation(String sourceLang, String targetLang, String text) {
        return translationCacheService.get(sourceLang, targetLang, text);
    }

    /**
     * After a translation lands for {@code locale}, redraw chat for every online
     * receiver whose locale matches — they had the original; now they see the
     * translation.
     */
    private void replayForLocale(String locale) {
        if (locale == null) return;

        fPlayerService.getOnlineFPlayers().stream()
                .filter(player -> locale.equals(socialService.getSetting(player, SettingText.LOCALE)))
                .forEach(player -> sendUpdate(player.uuid()));
    }

    // ---- Global chat history & per-player replay ----

    /**
     * Store the chat event in the global history, adding this receiver to the
     * viewer set of the entry. If the message UUID already exists (because
     * another receiver saved it first), we just add this receiver to viewers;
     * the Component itself is shared.
     */
    public void save(FPlayer receiver,
                     UUID messageUUID,
                     Component component,
                     String originalText,
                     @Nullable TranslatedMessage translatedMessage,
                     boolean needToCache) {
        if (receiver.isUnknown()) {
            fLogger.debug("[History.save] skip — receiver is UNKNOWN (console etc), uuid=%s", messageUUID);
            return;
        }
        if (!receiver.isOnline()) {
            fLogger.debug("[History.save] skip — receiver=%s is offline, uuid=%s", receiver.name(), messageUUID);
            return;
        }

        UUID receiverUUID = receiver.uuid();
        String receiverLocale = socialService.getSetting(receiver, SettingText.LOCALE);

        int newGlobalSize;
        boolean wasExisting;
        int newViewersCount;
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
                // Merge translatedMessage into existing entry. Save() is called multiple
                // times per messageUUID (once per receiver). The first call may have a
                // null TranslatedMessage (e.g. for the sender themselves whose locale
                // matches the source so no translation was synthesized); a later call
                // for another receiver may have synthesized one from cache. Without this
                // merge the entry would be stuck with the first save's null TM and
                // toggle would report 'has no translations'.
                if (translatedMessage != null) {
                    if (existing.translatedMessage() == null) {
                        // Replace the entry in-place — record fields are final.
                        TranslateHistoryMessage updated = existing.withTranslatedMessage(translatedMessage);
                        int idx = globalHistory.indexOf(existing);
                        if (idx >= 0) globalHistory.set(idx, updated);
                        existing = updated;
                    } else {
                        // Both non-null — merge per-locale translations into the
                        // existing TranslatedMessage's mutable translations map.
                        existing.translatedMessage().translations().putAll(translatedMessage.translations());
                    }
                }
                wasExisting = true;
                newViewersCount = existing.viewers().size();
            } else {
                TranslateHistoryMessage entry = TranslateHistoryMessage.create(
                        messageUUID, receiverLocale, component, originalText, translatedMessage
                );
                entry.viewers().add(receiverUUID);
                globalHistory.add(entry);
                while (globalHistory.size() > historyLength()) {
                    globalHistory.remove(0);
                }
                wasExisting = false;
                newViewersCount = entry.viewers().size();
            }
            newGlobalSize = globalHistory.size();
        }

        fLogger.debug("[History.save] uuid=%s receiver=%s locale=%s text='%s' hasTranslatedMessage=%s mode=%s viewersNow=%d globalSize=%d",
                messageUUID, receiver.name(), receiverLocale, originalText,
                translatedMessage != null, wasExisting ? "UPDATE" : "CREATE",
                newViewersCount, newGlobalSize);

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

    /**
     * Register a Component as plugin-originated for ReceiveEvent dedup.
     *
     * <p>Caller must pass the EXACT Component instance that will be carried by
     * the outgoing packet — i.e. the result of any prior {@code event.withMessage(...)}
     * substitution, not the pre-modification original. PacketEvents forwards
     * the same reference back via {@code wrapper.getMessage()}; {@link #isCached}
     * compares by reference, so passing the wrong variant silently breaks dedup
     * and the listener saves a duplicate history entry.
     */
    public void markSelfOriginated(Component sentComponent) {
        if (sentComponent == null) return;
        if (!selfOriginatedComponents.contains(sentComponent)) {
            selfOriginatedComponents.add(sentComponent);
        }
    }

    /**
     * Player left — drop them from every viewer set and discard their toggle state.
     * Entries themselves stay (other players may still view them).
     */
    public void clearHistory(FPlayer fPlayer) {
        UUID playerUUID = fPlayer.uuid();
        int affectedEntries = 0;
        synchronized (globalHistory) {
            for (TranslateHistoryMessage entry : globalHistory) {
                if (entry.viewers().remove(playerUUID)) {
                    affectedEntries++;
                }
            }
        }
        Set<UUID> previousToggles = playerOriginalToggles.remove(playerUUID);
        fLogger.debug("[History.clear] player=%s — removed from %d viewer set(s), discarded %d toggle state(s)",
                fPlayer.name(), affectedEntries,
                previousToggles == null ? 0 : previousToggles.size());
    }

    /**
     * Brute-force chat redraw for one receiver: filter the global history to
     * entries this receiver saw, fill any missing translations from the global
     * cache, then push enough empty newlines to scroll old chat off-screen
     * before reprinting.
     */
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
            fLogger.debug("[Toggle] sendUpdate: nothing to redraw for player=%s (no entries with this viewer)",
                    fPlayer.name());
            return;
        }

        fillTranslationsFromCache(visible, playerLocale);

        Set<UUID> toggles = playerOriginalToggles.getOrDefault(receiverUUID, java.util.Set.of());

        int len = historyLength();
        int emptyLines = Math.max(0, len - visible.size());
        fLogger.debug("[Toggle] sendUpdate: player=%s locale=%s visibleEntries=%d emptyLines=%d globalSize=%d",
                fPlayer.name(), playerLocale, visible.size(), emptyLines,
                globalHistorySize());

        for (int i = 0; i < emptyLines; i++) {
            messageSender.sendMessage(fPlayer, Component.newline(), true);
        }
        int idx = 0;
        for (TranslateHistoryMessage entry : visible) {
            boolean showOriginal = toggles.contains(entry.uuid());
            String translationText = entry.translatedMessage() != null
                    ? entry.translatedMessage().getTranslation(playerLocale)
                    : null;
            fLogger.debug("[Toggle] sendUpdate:  [%d/%d] uuid=%s text='%s' showOriginal=%s translationForLocale=%s",
                    idx++, visible.size(), entry.uuid(), entry.originalText(),
                    showOriginal, translationText == null ? "<none>" : "'" + translationText + "'");
            messageSender.sendMessage(fPlayer, entry.getDisplayComponent(playerLocale, showOriginal), true);
        }
    }

    private int globalHistorySize() {
        synchronized (globalHistory) {
            return globalHistory.size();
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
                fLogger.debug("[Toggle] sendUpdate: filled %s→%s translation from global cache for uuid=%s = '%s'",
                        tm.originalLang(), receiverLocale, entry.uuid(), cached);
            }
        }
    }

    public boolean toggleOriginal(FPlayer fPlayer, UUID messageUUID) {
        fLogger.debug("[Toggle] toggleOriginal called: player=%s uuid=%s",
                fPlayer == null ? "null" : fPlayer.name(), messageUUID);

        if (moduleController.isDisabledFor(this, fPlayer)) {
            fLogger.debug("[Toggle] skip: module disabled for player=%s",
                    fPlayer == null ? "null" : fPlayer.name());
            return false;
        }
        if (messageUUID == null) {
            fLogger.debug("[Toggle] skip: messageUUID is null");
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
            fLogger.debug("[Toggle] skip: entry uuid=%s not found in global history", messageUUID);
            return false;
        }
        if (!entry.viewers().contains(playerUUID)) {
            fLogger.debug("[Toggle] skip: player=%s did not see uuid=%s", fPlayer.name(), messageUUID);
            return false;
        }
        if (!entry.hasTranslations()) {
            fLogger.debug("[Toggle] skip: entry uuid=%s has no translations (server/system message)", messageUUID);
            return false;
        }

        Set<UUID> toggles = playerOriginalToggles.computeIfAbsent(playerUUID, _ -> ConcurrentHashMap.newKeySet());
        boolean wasShowingOriginal = toggles.contains(messageUUID);
        boolean nowShowingOriginal = !wasShowingOriginal;
        if (nowShowingOriginal) toggles.add(messageUUID);
        else toggles.remove(messageUUID);

        fLogger.debug("[Toggle] flipped player=%s showOriginal for uuid=%s: %s → %s (originalText='%s')",
                fPlayer.name(), messageUUID, wasShowingOriginal, nowShowingOriginal, entry.originalText());

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

        fLogger.debug("[Toggle] no usable translation for %s→%s on uuid=%s, kicking off retry via chain",
                tm.originalLang(), playerLocale, entry.uuid());

        UUID receiverUUID = fPlayer.uuid();
        translationCacheService.translateAsync(tm.originalLang(), playerLocale, entry.originalText(), config().providers())
                .thenAccept(result -> {
                    if (result == null || result.isEmpty() || result.equals(entry.originalText())) {
                        fLogger.debug("[Toggle] retry %s→%s for uuid=%s produced no usable translation",
                                tm.originalLang(), playerLocale, entry.uuid());
                        return;
                    }
                    tm.translations().put(playerLocale, result);
                    fLogger.debug("[Toggle] retry %s→%s for uuid=%s succeeded with '%s', redrawing",
                            tm.originalLang(), playerLocale, entry.uuid(), result);
                    sendUpdate(receiverUUID);
                });
    }

    /**
     * History length — borrows the value from DeleteModule's config to keep behavior
     * consistent (both modules push out the same number of empty lines).
     */
    private int historyLength() {
        return fileFacade.message().format().moderation().delete().historyLength();
    }

    // ---- Original message lookup by UUID (kept from author's code) ----

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
