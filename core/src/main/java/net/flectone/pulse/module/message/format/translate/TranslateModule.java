package net.flectone.pulse.module.message.format.translate;

import com.google.common.cache.Cache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
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
     * Auto-translate chat history kept inside TranslateModule — by analogy with
     * DeleteModule's playersHistory but fully independent. Each module owns its
     * own state; no shared service.
     */
    private final Map<UUID, List<TranslateHistoryMessage>> playersHistory = new ConcurrentHashMap<>();

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

    @Override
    public void onEnable() {
        listenerRegistry.register(PulseTranslateListener.class);
        listenerRegistry.register(PulseAutoTranslateListener.class);
    }

    @Override
    public void onDisable() {
        messageCache.invalidateAll();
        playersHistory.clear();
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
    public Localization.Message.Format.Translate localization(FEntity sender) {
        return fileFacade.localization(sender).message().format().translate();
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
     * Adds the {@code <translation>} tag resolver. The button is hidden when the
     * receiver shares the sender's locale (no translation will be done for them,
     * so there's nothing to toggle).
     */
    public MessageContext addTag(MessageContext messageContext) {
        FEntity sender = messageContext.sender();
        if (moduleController.isDisabledFor(this, sender)) return messageContext;

        FPlayer receiver = messageContext.receiver();

        return messageContext.addTagResolver(MessagePipeline.ReplacementTag.TRANSLATION, (_, _) -> {
            // Only player-to-player messages go through auto-translate — hide button otherwise
            if (!(sender instanceof FPlayer fSender)) {
                return Tag.selfClosingInserting(Component.empty());
            }
            String senderLocale = fSender.getSetting(SettingText.LOCALE);
            String receiverLocale = receiver != null ? receiver.getSetting(SettingText.LOCALE) : null;

            // Same-locale receiver — no translation, no button
            if (senderLocale != null && senderLocale.equals(receiverLocale)) {
                return Tag.selfClosingInserting(Component.empty());
            }

            UUID messageUUID = messageContext.messageUUID();

            String action = localization(receiver).action();
            action = Strings.CS.replace(action, "<message>", messageUUID.toString());

            MessageContext tagContext = messagePipeline.createContext(sender, receiver, action)
                    .withFlags(messageContext.flags())
                    .addFlags(
                            new MessageFlag[]{MessageFlag.MENTION_MODULE, MessageFlag.INTERACTIVE_CHAT_COMPAT, MessageFlag.QUESTIONANSWER_MODULE, MessageFlag.TRANSLATE_MODULE, MessageFlag.PLAYER_MESSAGE},
                            new boolean[]{false, false, false, false, false}
                    );

            return Tag.selfClosingInserting(messagePipeline.build(tagContext));
        });
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
                .map(player -> player.getSetting(SettingText.LOCALE))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        uniqueLocales.add(sourceLang);

        fLogger.info("[AutoTranslate] translateToAllLocales: source=%s text='%s' unique locales=%s",
                sourceLang, originalText, uniqueLocales);

        if (uniqueLocales.size() <= 1) {
            fLogger.info("[AutoTranslate] translateToAllLocales: skip — only one locale online");
            return null;
        }

        Map<String, Component> translations = new ConcurrentHashMap<>();
        translations.put(sourceLang, Component.text(originalText));

        TranslatedMessage translatedMessage = TranslatedMessage.builder()
                .originalText(originalText)
                .originalLang(sourceLang)
                .translations(translations)
                .build();

        List<String> targetLangs = uniqueLocales.stream()
                .filter(targetLang -> !targetLang.equals(sourceLang))
                .toList();

        fLogger.info("[AutoTranslate] translateToAllLocales: launching %d async translation(s) %s → %s",
                targetLangs.size(), sourceLang, targetLangs);

        targetLangs.forEach(targetLang ->
                translationCacheService.translateWithMyMemoryAsync(sourceLang, targetLang, originalText)
                        .thenAccept(translated -> {
                            Component translatedComponent = (translated != null && !translated.isEmpty())
                                    ? Component.text(translated)
                                    : Component.text(originalText);
                            translations.put(targetLang, translatedComponent);
                            fLogger.info("[AutoTranslate] translation arrived %s→%s, triggering replay for matching receivers",
                                    sourceLang, targetLang);
                            replayForLocale(targetLang);
                        })
                        .exceptionally(throwable -> {
                            fLogger.warning(throwable, "[AutoTranslate] translation %s failed, falling back to original",
                                    sourceLang + "→" + targetLang);
                            translations.put(targetLang, Component.text(originalText));
                            return null;
                        })
        );

        return translatedMessage;
    }

    /**
     * After a translation lands for {@code locale}, redraw chat for every online
     * receiver whose locale matches — they had the original; now they see the
     * translation.
     */
    private void replayForLocale(String locale) {
        if (locale == null) return;

        fPlayerService.getOnlineFPlayers().stream()
                .filter(player -> locale.equals(player.getSetting(SettingText.LOCALE)))
                .forEach(player -> sendUpdate(player.uuid()));
    }

    // ---- Self-contained chat history & replay (mirrors DeleteModule pattern) ----

    public void save(FPlayer receiver, UUID messageUUID, Component component, boolean needToCache) {
        save(receiver, messageUUID, component, null, needToCache);
    }

    public void save(FPlayer receiver,
                     UUID messageUUID,
                     Component component,
                     @Nullable TranslatedMessage translatedMessage,
                     boolean needToCache) {
        if (receiver.isUnknown()) return;
        if (!receiver.isOnline()) return;

        UUID playerUUID = receiver.uuid();
        TranslateHistoryMessage entry = new TranslateHistoryMessage(messageUUID, component, translatedMessage);

        List<TranslateHistoryMessage> history = playersHistory.computeIfAbsent(playerUUID, _ -> new ObjectArrayList<>());
        if (history.stream().anyMatch(h -> h.uuid().equals(messageUUID))) return;

        if (history.size() >= historyLength()) {
            history.removeFirst();
        }
        history.add(entry);

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

    public void clearHistory(FPlayer fPlayer) {
        playersHistory.remove(fPlayer.uuid());
    }

    /**
     * Brute-force chat redraw — spam empty newlines to push old chat off-screen,
     * then reprint history with each entry rendering its current state
     * (original vs translation based on showOriginal + receiver locale).
     */
    public void sendUpdate(UUID receiverUUID) {
        List<TranslateHistoryMessage> history = playersHistory.get(receiverUUID);
        if (history == null) return;

        FPlayer fPlayer = fPlayerService.getFPlayer(receiverUUID);
        String playerLocale = fPlayer.getSetting(SettingText.LOCALE);

        int len = historyLength();
        for (int i = 0; i < len; i++) {
            if (i >= history.size()) {
                messageSender.sendMessage(fPlayer, Component.newline(), true);
            }
        }
        history.forEach(entry ->
                messageSender.sendMessage(fPlayer, entry.getDisplayComponent(playerLocale), true)
        );
    }

    public boolean toggleOriginal(FPlayer fPlayer, UUID messageUUID) {
        if (moduleController.isDisabledFor(this, fPlayer)) return false;
        if (messageUUID == null) return false;

        UUID playerUUID = fPlayer.uuid();
        List<TranslateHistoryMessage> history = playersHistory.get(playerUUID);
        if (history == null) return false;

        boolean updated = false;
        for (int i = 0; i < history.size(); i++) {
            TranslateHistoryMessage entry = history.get(i);
            if (messageUUID.equals(entry.uuid()) && entry.hasTranslations()) {
                history.set(i, entry.withShowOriginal(!entry.showOriginal()));
                updated = true;
                break;
            }
        }

        if (updated) {
            sendUpdate(playerUUID);
        }
        return updated;
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
                .findFirst()
                .map(Map.Entry::getKey)
                .orElse(null);
    }
}
