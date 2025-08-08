package net.flectone.pulse.module.message.format.replacement.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.module.message.format.replacement.ReplacementModule;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageFlag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;

@Singleton
public class ReplacementPulseListener implements PulseListener {

    private final Message.Format.Replacement message;
    private final ReplacementModule replacementModule;
    private final MessagePipeline messagePipeline;

    @Inject
    public ReplacementPulseListener(FileResolver fileResolver,
                                    ReplacementModule replacementModule,
                                    MessagePipeline messagePipeline) {
        this.message = fileResolver.getMessage().getFormat().getReplacement();
        this.replacementModule = replacementModule;
        this.messagePipeline = messagePipeline;
    }

    @Pulse
    public void onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.getContext();
        if (!messageContext.isFlag(MessageFlag.REPLACEMENT)) return;

        FEntity sender = messageContext.getSender();
        if (replacementModule.isModuleDisabledFor(sender)) return;

        String processedMessage = replacementModule.processMessage(messageContext.getMessage());
        messageContext.setMessage(processedMessage);

        FPlayer receiver = messageContext.getReceiver();
        messageContext.addReplacementTag(MessagePipeline.ReplacementTag.REPLACEMENT, (argumentQueue, context) -> {
            Tag.Argument argument = argumentQueue.peek();
            if (argument == null) return Tag.selfClosingInserting(Component.empty());

            String currentValue = argument.value();
            Message.Format.Replacement.ReplacementValue replacementValue = message.getValues()
                    .stream()
                    .filter(value -> value.getTrigger().equalsIgnoreCase(currentValue))
                    .findAny()
                    .orElse(null);
            if (replacementValue == null) return Tag.selfClosingInserting(Component.empty());

            Component component = messagePipeline.builder(sender, receiver, replacementValue.getReplace())
                    .flag(MessageFlag.REPLACEMENT, false)
                    .build();

            return Tag.selfClosingInserting(component);
        });
    }

}
