package net.flectone.pulse.module.message.format.emoji;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.formatter.MessageFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static net.flectone.pulse.util.TagResolverUtil.emptyTagResolver;

@Singleton
public class EmojiModule extends AbstractModule {

    private final Message.Format.Emoji message;
    private final Permission.Message.Format.Emoji permission;
    private final MessageFormatter messageFormatter;

    @Inject
    public EmojiModule(FileManager fileManager,
                       MessageFormatter messageFormatter) {
        this.messageFormatter = messageFormatter;

        message = fileManager.getMessage().getFormat().getEmoji();
        permission = fileManager.getPermission().getMessage().getFormat().getEmoji();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);
    }

    public TagResolver emojiTag(FEntity fPlayer, FEntity receiver) {
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

            Component component = messageFormatter.builder(fPlayer, receiver, emojis.getValue())
                    .emoji(false)
                    .build();

            return Tag.selfClosingInserting(component);
        });
    }

    public String replace(@Nullable FEntity sender, String message) {
        if (checkModulePredicates(sender)) return message;

        for (Map.Entry<String, String> emoji : this.message.getValues().entrySet()) {
            message = message.replace(emoji.getKey(), "<emoji:'" + emoji.getKey() + "'>");
        }

        return message;
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }
}
