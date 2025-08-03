package net.flectone.pulse.module.message.format.emoji.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.module.message.format.emoji.EmojiModule;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;

import java.util.Map;

@Singleton
public class EmojiPulseListener implements PulseListener {

    private final Message.Format.Emoji message;
    private final EmojiModule emojiModule;
    private final MessagePipeline messagePipeline;

    @Inject
    public EmojiPulseListener(FileResolver fileResolver,
                              EmojiModule emojiModule,
                              MessagePipeline messagePipeline) {
        this.message = fileResolver.getMessage().getFormat().getEmoji();
        this.emojiModule = emojiModule;
        this.messagePipeline = messagePipeline;
    }

    @Pulse
    public void onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.getContext();
        if (!messageContext.isFlag(MessageFlag.EMOJI)) return;

        FEntity sender = messageContext.getSender();
        if (emojiModule.checkModulePredicates(sender)) return;

        String processedMessage = replace(messageContext.getMessage());
        messageContext.setMessage(processedMessage);

        FPlayer receiver = messageContext.getReceiver();
        messageContext.addReplacementTag(MessagePipeline.ReplacementTag.EMOJI, (argumentQueue, context) -> {
            Tag.Argument emojiTag = argumentQueue.peek();
            if (emojiTag == null) return Tag.selfClosingInserting(Component.empty());

            String currentEmoji = emojiTag.value();

            Map.Entry<String, String> emojis = message.getValues()
                    .entrySet()
                    .stream()
                    .filter(entry -> entry.getKey().equals(currentEmoji))
                    .findAny()
                    .orElse(null);

            if (emojis == null) return Tag.selfClosingInserting(Component.empty());

            Component component = messagePipeline.builder(sender, receiver, emojis.getValue())
                    .flag(MessageFlag.EMOJI, false)
                    .build();

            return Tag.selfClosingInserting(component);
        });
    }

    private String replace(String string) {
        for (Map.Entry<String, String> emoji : message.getValues().entrySet()) {
            string = string.replace(emoji.getKey(), "<emoji:'" + emoji.getKey() + "'>");
        }

        return string;
    }

}
