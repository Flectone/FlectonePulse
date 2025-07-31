package net.flectone.pulse.module.message.format.spoiler.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.constant.MessageFlag;
import net.flectone.pulse.context.MessageContext;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.module.message.format.spoiler.SpoilerModule;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.resolver.FileResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

@Singleton
public class SpoilerPulseListener implements PulseListener {

    private final Message.Format.Spoiler message;
    private final SpoilerModule spoilerModule;
    private final MessagePipeline messagePipeline;

    @Inject
    public SpoilerPulseListener(FileResolver fileResolver,
                                SpoilerModule spoilerModule,
                                MessagePipeline messagePipeline) {
        this.message = fileResolver.getMessage().getFormat().getSpoiler();
        this.spoilerModule = spoilerModule;
        this.messagePipeline = messagePipeline;
    }

    @Pulse
    public void onMessageProcessingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.getContext();
        if (!messageContext.isFlag(MessageFlag.SPOILER)) return;

        FEntity sender = messageContext.getSender();
        if (spoilerModule.checkModulePredicates(sender)) return;

        boolean userMessage = messageContext.isFlag(MessageFlag.USER_MESSAGE);
        FPlayer receiver = messageContext.getReceiver();

        messageContext.addReplacementTag(MessagePipeline.ReplacementTag.SPOILER, (argumentQueue, context) -> {
            Tag.Argument spoilerTag = argumentQueue.peek();
            if (spoilerTag == null) return Tag.selfClosingInserting(Component.empty());

            String spoilerText = spoilerTag.value();

            Component spoilerComponent = messagePipeline.builder(sender, receiver, spoilerText)
                    .flag(MessageFlag.USER_MESSAGE, userMessage)
                    .build();

            int length = PlainTextComponentSerializer.plainText().serialize(spoilerComponent).length();

            Localization.Message.Format.Spoiler localization = spoilerModule.resolveLocalization(receiver);

            Component component = Component.text(localization.getSymbol().repeat(length))
                    .hoverEvent(messagePipeline.builder(sender, receiver, localization.getHover())
                            .build()
                            .replaceText(TextReplacementConfig.builder().match("<message>")
                                    .replacement(spoilerComponent)
                                    .build()
                            )
                    )
                    .color(messagePipeline.builder(sender, receiver, message.getColor())
                            .build()
                            .color()
                    );

            return Tag.selfClosingInserting(component);
        });
    }
}
