package net.flectone.pulse.module.message.format.emoji;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.context.MessageContext;
import net.flectone.pulse.processor.MessageProcessor;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.registry.MessageProcessRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static net.flectone.pulse.util.TagResolverUtil.emptyTagResolver;

@Singleton
public class EmojiModule extends AbstractModule implements MessageProcessor {

    private final Message.Format.Emoji message;
    private final Permission.Message.Format.Emoji permission;
    private final MessagePipeline messagePipeline;

    @Inject
    public EmojiModule(FileResolver fileResolver,
                       MessagePipeline messagePipeline,
                       MessageProcessRegistry messageProcessRegistry) {
        this.messagePipeline = messagePipeline;

        message = fileResolver.getMessage().getFormat().getEmoji();
        permission = fileResolver.getPermission().getMessage().getFormat().getEmoji();

        messageProcessRegistry.register(100, this);
    }

    @Override
    public void reload() {
        registerModulePermission(permission);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Override
    public void process(MessageContext messageContext) {
        if (!messageContext.isEmoji()) return;

        String message = replace(messageContext.getSender(), messageContext.getMessage());

        messageContext.setMessage(message);
        messageContext.addTagResolvers(emojiTag(messageContext.getSender(), messageContext.getReceiver()));
    }

    private String replace(@Nullable FEntity sender, String message) {
        if (checkModulePredicates(sender)) return message;

        for (Map.Entry<String, String> emoji : this.message.getValues().entrySet()) {
            message = message.replace(emoji.getKey(), "<emoji:'" + emoji.getKey() + "'>");
        }

        return message;
    }

    private TagResolver emojiTag(FEntity fPlayer, FEntity receiver) {
        String tag = "emoji";
        if (checkModulePredicates(fPlayer)) return emptyTagResolver(tag);

        return TagResolver.resolver(tag, (argumentQueue, context) -> {
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

            Component component = messagePipeline.builder(fPlayer, receiver, emojis.getValue())
                    .emoji(false)
                    .build();

            return Tag.selfClosingInserting(component);
        });
    }
}
