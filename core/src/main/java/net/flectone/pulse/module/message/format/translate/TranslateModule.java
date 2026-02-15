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
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.format.translate.listener.TranslatePulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TranslateModule extends AbstractModuleLocalization<Localization.Message.Format.Translate> {

    private final @Named("translateMessage") Cache<String, UUID> messageCache;
    private final FileFacade fileFacade;
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

    public MessageContext addTag(MessageContext messageContext, UUID key) {
        FEntity sender = messageContext.sender();
        if (isModuleDisabledFor(sender)) return messageContext;

        FPlayer receiver = messageContext.receiver();

        return messageContext.addTagResolver(Set.of(MessagePipeline.ReplacementTag.TRANSLATE), (argumentQueue, context) -> {
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

            String action = localization(receiver).action();
            action = Strings.CS.replaceOnce(action, "<language>", firstLang);
            action = Strings.CS.replaceOnce(action, "<language>", secondLang == null ? "ru_ru" : secondLang);
            action = Strings.CS.replace(action, "<message>", key.toString());

            MessageContext tagContext = messagePipeline.createContext(sender, receiver, action)
                    .withFlags(messageContext.flags())
                    .addFlags(
                            new MessageFlag[]{MessageFlag.MENTION, MessageFlag.INTERACTIVE_CHAT, MessageFlag.QUESTION, MessageFlag.TRANSLATE, MessageFlag.USER_MESSAGE},
                            new boolean[]{false, false, false, false, false}
                    );

            return Tag.selfClosingInserting(messagePipeline.build(tagContext));
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

    public @Nullable String getMessage(UUID uuid) {
        return messageCache.asMap().entrySet()
                .stream()
                .filter(entry -> entry.getValue().equals(uuid))
                .findFirst()
                .map(Map.Entry::getKey)
                .orElse(null);
    }
}
