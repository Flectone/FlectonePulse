package net.flectone.pulse.module.message.format.translate;

import com.google.common.cache.Cache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.format.translate.listener.TranslatePulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.SettingText;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.apache.commons.lang3.Strings;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TranslateModule extends AbstractModuleLocalization<Localization.Message.Format.Translate> {

    private final @Named("translateMessage") Cache<String, UUID> messageCache;
    private final FileResolver fileResolver;
    private final ListenerRegistry listenerRegistry;
    private final MessagePipeline messagePipeline;

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(TranslatePulseListener.class);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        messageCache.invalidateAll();
    }

    @Override
    public MessageType messageType() {
        return MessageType.TRANSLATE;
    }

    @Override
    public Message.Format.Translate config() {
        return fileResolver.getMessage().getFormat().getTranslate();
    }

    @Override
    public Permission.Message.Format.Translate permission() {
        return fileResolver.getPermission().getMessage().getFormat().getTranslate();
    }

    @Override
    public Localization.Message.Format.Translate localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getFormat().getTranslate();
    }

    public UUID saveMessage(String message) {
        UUID uuid = messageCache.getIfPresent(message);
        if (uuid == null) {
            uuid = UUID.randomUUID();
            messageCache.put(message, uuid);
        }

        return uuid;
    }

    public void addTag(MessageContext messageContext, UUID key) {
        FEntity sender = messageContext.getSender();
        if (isModuleDisabledFor(sender)) return;

        FPlayer receiver = messageContext.getReceiver();

        messageContext.addReplacementTag(Set.of(MessagePipeline.ReplacementTag.TRANSLATE, MessagePipeline.ReplacementTag.TRANSLATETO), (argumentQueue, context) -> {
            String firstLang = "auto";
            String secondLang = receiver.getSetting(SettingText.LOCALE);

            if (argumentQueue.hasNext()) {
                Tag.Argument first = argumentQueue.pop();

                if (argumentQueue.hasNext()) {
                    Tag.Argument second = argumentQueue.pop();

                    if (argumentQueue.hasNext()) {
                        // translateto language language message
                        firstLang = first.value();
                        secondLang = second.value();
                    } else {
                        // translateto auto language message
                        secondLang = first.value();
                    }
                }
            }

            String action = localization(receiver).getAction();
            action = Strings.CS.replaceOnce(action, "<language>", firstLang);
            action = Strings.CS.replaceOnce(action, "<language>", secondLang == null ? "ru_ru" : secondLang);
            action = Strings.CS.replace(action, "<message>", key.toString());

            Component component = messagePipeline.builder(sender, receiver, action)
                    .flag(MessageFlag.MENTION, false)
                    .flag(MessageFlag.INTERACTIVE_CHAT, false)
                    .flag(MessageFlag.QUESTION, false)
                    .flag(MessageFlag.TRANSLATE, false)
                    .build();

            return Tag.selfClosingInserting(component);
        });
    }

    public String getMessage(String stringUUID) {
        try {
            UUID uuid = UUID.fromString(stringUUID);

            return getMessage(uuid);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    @Nullable
    public String getMessage(UUID uuid) {
        return messageCache.asMap().entrySet()
                .stream()
                .filter(entry -> entry.getValue().equals(uuid))
                .findFirst()
                .map(Map.Entry::getKey)
                .orElse(null);
    }
}
