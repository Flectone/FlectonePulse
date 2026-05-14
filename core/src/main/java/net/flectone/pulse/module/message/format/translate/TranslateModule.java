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
import net.flectone.pulse.module.command.translateto.TranslatetoModule;
import net.flectone.pulse.module.message.format.translate.listener.PulseTranslateListener;
import net.flectone.pulse.module.message.format.translate.model.TranslatedMessage;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
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
import java.util.stream.Collectors;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TranslateModule implements ModuleLocalization<Localization.Message.Format.Translate> {

    private final @Named("translateMessage") Cache<String, UUID> messageCache;
    private final FileFacade fileFacade;
    private final ListenerRegistry listenerRegistry;
    private final MessagePipeline messagePipeline;
    private final ModuleController moduleController;
    private final UUIDParser uuidParser;
    private final FPlayerService fPlayerService;
    private final TranslatetoModule translatetoModule;
    private final TranslationCacheService translationCacheService;
    private final FLogger fLogger;

    @Override
    public void onEnable() {
        listenerRegistry.register(PulseTranslateListener.class);
        listenerRegistry.register(net.flectone.pulse.module.message.format.translate.listener.PulseAutoTranslateListener.class);
    }

    @Override
    public void onDisable() {
        messageCache.invalidateAll();
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

    public MessageContext addTag(MessageContext messageContext) {
        FEntity sender = messageContext.sender();
        FPlayer receiver = messageContext.receiver();

        if (moduleController.isDisabledFor(this, sender)) {
            fLogger.info("[Translate] addTag: skip — module disabled for sender=%s",
                    sender == null ? "null" : sender.name());
            return messageContext;
        }

        fLogger.info("[Translate] addTag: register <translation> resolver for sender=%s receiver=%s",
                sender == null ? "null" : sender.name(),
                receiver == null ? "null" : receiver.name());

        return messageContext.addTagResolver(MessagePipeline.ReplacementTag.TRANSLATION, (argumentQueue, _) -> {
            long uniqueLocaleCount = fPlayerService.getOnlineFPlayers().stream()
                    .map(player -> player.getSetting(SettingText.LOCALE))
                    .filter(Objects::nonNull)
                    .distinct()
                    .count();

            fLogger.info("[Translate] addTag.resolver: uniqueLocaleCount=%d receiver=%s",
                    uniqueLocaleCount, receiver == null ? "null" : receiver.name());

            if (uniqueLocaleCount <= 1) {
                fLogger.info("[Translate] addTag.resolver: hide button — only %d unique locale(s) online",
                        uniqueLocaleCount);
                return Tag.selfClosingInserting(Component.empty());
            }

            UUID messageUUID = messageContext.messageUUID();

            String action = localization(receiver).action();
            action = Strings.CS.replace(action, "<message>", messageUUID.toString());

            fLogger.info("[Translate] addTag.resolver: building button for uuid=%s", messageUUID);

            MessageContext tagContext = messagePipeline.createContext(sender, receiver, action)
                    .withFlags(messageContext.flags())
                    .addFlags(
                            new MessageFlag[]{MessageFlag.MENTION_MODULE, MessageFlag.INTERACTIVE_CHAT_COMPAT, MessageFlag.QUESTIONANSWER_MODULE, MessageFlag.TRANSLATE_MODULE, MessageFlag.PLAYER_MESSAGE},
                            new boolean[]{false, false, false, false, false}
                    );

            return Tag.selfClosingInserting(messagePipeline.build(tagContext));
        });
    }

    public @Nullable TranslatedMessage translateToAllLocales(String originalText, String sourceLang) {
        fLogger.info("[AutoTranslate] translateToAllLocales: enter source=%s text='%s'", sourceLang, originalText);

        if (moduleController.isDisabledFor(this, FPlayer.UNKNOWN)) {
            fLogger.info("[AutoTranslate] translateToAllLocales: skip — module disabled globally");
            return null;
        }

        Set<String> uniqueLocales = fPlayerService.getOnlineFPlayers().stream()
                .map(player -> player.getSetting(SettingText.LOCALE))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        int onlineCount = fPlayerService.getOnlineFPlayers().size();
        fLogger.info("[AutoTranslate] translateToAllLocales: online players=%d, unique locales from settings=%s",
                onlineCount, uniqueLocales);

        uniqueLocales.add(sourceLang);

        if (uniqueLocales.size() <= 1) {
            fLogger.info("[AutoTranslate] translateToAllLocales: skip — only %d unique locale(s) including source (%s)",
                    uniqueLocales.size(), sourceLang);
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

        fLogger.info("[AutoTranslate] translateToAllLocales: launching %d async translation(s): %s → %s",
                targetLangs.size(), sourceLang, targetLangs);

        List<CompletableFuture<Void>> futures = targetLangs.stream()
                .map(targetLang -> translationCacheService.translateWithMyMemoryAsync(sourceLang, targetLang, originalText)
                        .thenAccept(translated -> {
                            if (translated != null && !translated.isEmpty()) {
                                fLogger.info("[AutoTranslate] translateToAllLocales: %s→%s SUCCESS → '%s'",
                                        sourceLang, targetLang, translated);
                                translations.put(targetLang, Component.text(translated));
                            } else {
                                fLogger.info("[AutoTranslate] translateToAllLocales: %s→%s returned null/empty, fallback to original",
                                        sourceLang, targetLang);
                                translations.put(targetLang, Component.text(originalText));
                            }
                        })
                        .exceptionally(throwable -> {
                            fLogger.warning(throwable, "[AutoTranslate] translateToAllLocales: %s FAILED, fallback to original",
                                    sourceLang + "→" + targetLang);
                            translations.put(targetLang, Component.text(originalText));
                            return null;
                        })
                )
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        fLogger.info("[AutoTranslate] translateToAllLocales: returning TranslatedMessage (async tasks still running in background), current translations=%s",
                translations.keySet());

        return translatedMessage;
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
                .findFirst()
                .map(Map.Entry::getKey)
                .orElse(null);
    }
}
