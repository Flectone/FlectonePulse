package net.flectone.pulse.module.message.format.emoji;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.constant.MessageFlag;
import net.flectone.pulse.context.MessageContext;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.processor.MessageProcessor;
import net.flectone.pulse.registry.MessageProcessRegistry;
import net.flectone.pulse.resolver.FileResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;

import java.util.Map;

@Singleton
public class EmojiModule extends AbstractModule implements MessageProcessor {

    private final Message.Format.Emoji message;
    private final Permission.Message.Format.Emoji permission;
    private final MessagePipeline messagePipeline;
    private final MessageProcessRegistry messageProcessRegistry;

    @Inject
    public EmojiModule(FileResolver fileResolver,
                       MessagePipeline messagePipeline,
                       MessageProcessRegistry messageProcessRegistry) {
        this.message = fileResolver.getMessage().getFormat().getEmoji();
        this.permission = fileResolver.getPermission().getMessage().getFormat().getEmoji();
        this.messagePipeline = messagePipeline;
        this.messageProcessRegistry = messageProcessRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        messageProcessRegistry.register(100, this);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Override
    public void process(MessageContext messageContext) {
        if (!messageContext.isFlag(MessageFlag.EMOJI)) return;

        FEntity sender = messageContext.getSender();
        if (checkModulePredicates(sender)) return;

        String processedMessage = replace(messageContext.getMessage());
        messageContext.setMessage(processedMessage);

        FPlayer receiver = messageContext.getReceiver();
        messageContext.addReplacementTag(MessagePipeline.ReplacementTag.EMOJI, (argumentQueue, context) -> {
            Tag.Argument emojiTag = argumentQueue.peek();
            if (emojiTag == null) return Tag.selfClosingInserting(Component.empty());

            String currentEmoji = emojiTag.value();

            var emojis = message.getValues()
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

    private String replace(String message) {
        for (Map.Entry<String, String> emoji : this.message.getValues().entrySet()) {
            message = message.replace(emoji.getKey(), "<emoji:'" + emoji.getKey() + "'>");
        }

        return message;
    }
}
