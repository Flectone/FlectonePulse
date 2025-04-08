package net.flectone.pulse.module.message.format.translate;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.context.MessageContext;
import net.flectone.pulse.processor.MessageProcessor;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.registry.MessageProcessRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import static net.flectone.pulse.util.TagResolverUtil.emptyTagResolver;

@Singleton
public class TranslateModule extends AbstractModuleMessage<Localization.Message.Format.Translate> implements MessageProcessor {

    private final Message.Format.Translate message;
    private final Permission.Message.Format.Translate permission;

    private final MessagePipeline messagePipeline;

    @Inject
    public TranslateModule(FileManager fileManager,
                           MessagePipeline messagePipeline,
                           MessageProcessRegistry messageProcessRegistry) {
        super(localization -> localization.getMessage().getFormat().getTranslate());

        this.messagePipeline = messagePipeline;

        message = fileManager.getMessage().getFormat().getTranslate();
        permission = fileManager.getPermission().getMessage().getFormat().getTranslate();

        messageProcessRegistry.register(100, this);
    }

    @Override
    public void reload() {
        registerModulePermission(permission);
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    @Override
    public void process(MessageContext messageContext) {
        if (!messageContext.isTranslate()) return;

        String messageToTranslate = messageContext.getMessageToTranslate();
        String message = messageContext.getMessage().replace("<message_to_translate>", messageToTranslate == null ? "" : messageToTranslate);

        messageContext.setMessage(message);
        messageContext.addTagResolvers(translateTag(messageContext.getSender(), messageContext.getReceiver()));
    }

    private TagResolver translateTag(FEntity fPlayer, FEntity receiver) {
        String tag = "translateto";
        if (checkModulePredicates(fPlayer)) return emptyTagResolver(tag);

        return TagResolver.resolver(tag, (argumentQueue, context) -> {
            if (!(receiver instanceof FPlayer fReceiver) || fReceiver.isUnknown()) return Tag.selfClosingInserting(Component.empty());
            if (!argumentQueue.hasNext()) return Tag.selfClosingInserting(Component.empty());

            String firstLang;
            String secondLang;
            String text;

            Tag.Argument first = argumentQueue.pop();

            if (argumentQueue.hasNext()) {
                Tag.Argument second = argumentQueue.pop();

                if (argumentQueue.hasNext()) {
                    // translateto language language message
                    firstLang = first.value();
                    secondLang = second.value();
                    text = argumentQueue.pop().value();
                } else {
                    // translateto auto language message
                    firstLang = "auto";
                    secondLang = first.value();
                    text = second.value();
                }
            } else {
                // translate auto language message
                firstLang = "auto";
                secondLang = fReceiver.getSettingValue(FPlayer.Setting.LOCALE);
                text = first.value();
            }

            String action = resolveLocalization(receiver).getAction()
                    .replaceFirst("<language>", firstLang)
                    .replaceFirst("<language>", secondLang)
                    .replace("<message>", text);

            return Tag.selfClosingInserting(messagePipeline.builder(fPlayer, receiver, action)
                    .interactiveChat(false)
                    .question(false)
                    .mention(false)
                    .translate(false)
                    .build()
            );
        });
    }
}
