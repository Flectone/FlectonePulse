package net.flectone.pulse.module.message.format.translate;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.constant.MessageFlag;
import net.flectone.pulse.context.MessageContext;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.processor.MessageProcessor;
import net.flectone.pulse.registry.MessageProcessRegistry;
import net.flectone.pulse.resolver.FileResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Singleton
public class TranslateModule extends AbstractModuleMessage<Localization.Message.Format.Translate> implements MessageProcessor {

    private final Cache<String, UUID> messageCache = CacheBuilder.newBuilder()
            .maximumSize(5000)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build();

    private final Message.Format.Translate message;
    private final Permission.Message.Format.Translate permission;
    private final MessagePipeline messagePipeline;
    private final MessageProcessRegistry messageProcessRegistry;

    @Inject
    public TranslateModule(FileResolver fileResolver,
                           MessagePipeline messagePipeline,
                           MessageProcessRegistry messageProcessRegistry) {
        super(localization -> localization.getMessage().getFormat().getTranslate());

        this.message = fileResolver.getMessage().getFormat().getTranslate();
        this.permission = fileResolver.getPermission().getMessage().getFormat().getTranslate();
        this.messagePipeline = messagePipeline;
        this.messageProcessRegistry = messageProcessRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        messageProcessRegistry.register(100, this);
    }

    @Override
    public void onDisable() {
        messageCache.invalidateAll();
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Override
    public void process(MessageContext messageContext) {
        if (!messageContext.isFlag(MessageFlag.TRANSLATE)) return;

        String messageToTranslate = messageContext.getMessageToTranslate();
        UUID key = saveMessage(messageToTranslate);

        FEntity sender = messageContext.getSender();
        if (checkModulePredicates(sender)) return;

        FEntity receiver = messageContext.getReceiver();

        messageContext.addReplacementTag(Set.of(MessagePipeline.ReplacementTag.TRANSLATE, MessagePipeline.ReplacementTag.TRANSLATETO), (argumentQueue, context) -> {
            String firstLang = "auto";
            String secondLang = receiver instanceof FPlayer fReceiver
                    ? fReceiver.getSettingValue(FPlayer.Setting.LOCALE)
                    : null;

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

            String action = resolveLocalization(receiver).getAction()
                    .replaceFirst("<language>", firstLang)
                    .replaceFirst("<language>", secondLang == null ? "ru_ru" : secondLang)
                    .replace("<message>", key.toString());

            Component component = messagePipeline.builder(sender, receiver, action)
                    .flag(MessageFlag.MENTION, false)
                    .flag(MessageFlag.INTERACTIVE_CHAT, false)
                    .flag(MessageFlag.QUESTION, false)
                    .flag(MessageFlag.TRANSLATE, false)
                    .build();

            return Tag.selfClosingInserting(component);
        });
    }

    private UUID saveMessage(String message) {
        UUID uuid = messageCache.getIfPresent(message);
        if (uuid == null) {
            uuid = UUID.randomUUID();
            messageCache.put(message, uuid);
        }

        return uuid;
    }

    public String getMessage(String stringUUID) {
        try {
            UUID uuid = UUID.fromString(stringUUID);

            return getMessage(uuid);
        } catch (IllegalArgumentException ignored) {
        }

        return null;
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
