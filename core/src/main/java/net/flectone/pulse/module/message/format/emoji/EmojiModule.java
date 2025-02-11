package net.flectone.pulse.module.message.format.emoji;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.util.ComponentUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@Singleton
public class EmojiModule extends AbstractModule {

    private final Message.Format.Emoji message;
    private final Permission.Message.Format.Emoji permission;

    @Inject private ComponentUtil componentUtil;

    @Inject
    public EmojiModule(FileManager fileManager) {
        message = fileManager.getMessage().getFormat().getEmoji();
        permission = fileManager.getPermission().getMessage().getFormat().getEmoji();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);
    }

    public TagResolver emojiTag(FEntity fPlayer, FEntity receiver) {
        if (checkModulePredicates(fPlayer)) return TagResolver.empty();

        return TagResolver.resolver("emoji", (argumentQueue, context) -> {
            Tag.Argument emojiTag = argumentQueue.peek();
            if (emojiTag == null) return Tag.selfClosingInserting(Component.empty());

            String currentEmoji = emojiTag.value();

            var emojis = message.getValues()
                    .entrySet()
                    .stream()
                    .filter(tag -> tag.getKey().equals(currentEmoji))
                    .findAny()
                    .orElse(null);

            if (emojis == null) return Tag.selfClosingInserting(Component.empty());

            Component component = componentUtil.builder(fPlayer, receiver, emojis.getValue())
                    .emoji(false)
                    .build();

            return Tag.selfClosingInserting(component);
        });
    }

    public String replace(@Nullable FEntity sender, String message) {
        if (checkModulePredicates(sender)) return message;

        for (Map.Entry<String, String> emoji : this.message.getValues().entrySet()) {
            message = message.replace(emoji.getKey(), "<emoji:\"" + emoji.getKey() + "\">");
        }

        return message;
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }
}
