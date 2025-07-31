package net.flectone.pulse.module.message.format.translate.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.constant.MessageFlag;
import net.flectone.pulse.context.MessageContext;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.module.message.format.translate.TranslateModule;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;

import java.util.Set;
import java.util.UUID;

@Singleton
public class TranslatePulseListener implements PulseListener {

    private final TranslateModule translateModule;
    private final MessagePipeline messagePipeline;

    @Inject
    public TranslatePulseListener(TranslateModule translateModule,
                                  MessagePipeline messagePipeline) {
        this.translateModule = translateModule;
        this.messagePipeline = messagePipeline;
    }

    @Pulse
    public void onMessageProcessingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.getContext();
        if (!messageContext.isFlag(MessageFlag.TRANSLATE)) return;

        String messageToTranslate = messageContext.getMessageToTranslate();
        UUID key = translateModule.saveMessage(messageToTranslate);

        FEntity sender = messageContext.getSender();
        if (translateModule.checkModulePredicates(sender)) return;

        FPlayer receiver = messageContext.getReceiver();

        messageContext.addReplacementTag(Set.of(MessagePipeline.ReplacementTag.TRANSLATE, MessagePipeline.ReplacementTag.TRANSLATETO), (argumentQueue, context) -> {
            String firstLang = "auto";
            String secondLang = receiver.getSettingValue(FPlayer.Setting.LOCALE);

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

            String action = translateModule.resolveLocalization(receiver).getAction()
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

}
