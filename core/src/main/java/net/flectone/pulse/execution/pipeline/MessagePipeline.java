package net.flectone.pulse.execution.pipeline;

import com.google.gson.JsonElement;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.TagPattern;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.intellij.lang.annotations.Subst;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MessagePipeline {

    private final FLogger fLogger;
    private final MiniMessage miniMessage;
    private final EventDispatcher eventDispatcher;

    public MessageContext createContext(@NonNull String message) {
        return createContext(FPlayer.UNKNOWN, message);
    }

    public MessageContext createContext(@NonNull FPlayer sender, @NonNull String message) {
        return createContext(sender, sender, message);
    }

    public MessageContext createContext(@NonNull FEntity sender, @NonNull FPlayer receiver) {
        return new MessageContext(UUID.randomUUID(), sender, receiver, null);
    }

    public MessageContext createContext(@NonNull FEntity sender, @NonNull FPlayer receiver, @NonNull String message) {
        return new MessageContext(UUID.randomUUID(), sender, receiver, message);
    }

    public MessageContext createContext(UUID messageUUID, @NonNull FEntity sender, @NonNull FPlayer receiver, @NonNull String message) {
        return new MessageContext(messageUUID, sender, receiver, message);
    }

    public Component build(MessageContext context) {
        // no need to build empty message
        if (StringUtils.isEmpty(context.message())) return Component.empty();

        MessageFormattingEvent event = eventDispatcher.dispatch(new MessageFormattingEvent(context));
        MessageContext eventContext = event.context();

        if (eventContext.isFlag(MessageFlag.REPLACE_DISABLED_TAGS) && !eventContext.isFlag(MessageFlag.USER_MESSAGE)) {
            TagResolver tagResolver = eventContext.tagResolver();
            eventContext = eventContext.addTagResolvers(Arrays.stream(ReplacementTag.values())
                    .filter(tag -> !tagResolver.has(tag.getTagName()))
                    .map(ReplacementTag::emptyResolver)
                    .toList()
            );
        }

        try {
            return miniMessage.deserialize(eventContext.message(), eventContext.tagResolver());
        } catch (Exception e) {
            fLogger.warning(e);
        }

        return Component.empty();
    }

    public String buildDefault(MessageContext context) {
        return addTrailingSpaces(context.message(), MiniMessage.miniMessage().serialize(build(context)));
    }

    public String buildPlain(MessageContext context) {
        return addTrailingSpaces(context.message(), PlainTextComponentSerializer.plainText().serialize(build(context)));
    }

    public String buildLegacy(MessageContext context) {
        return addTrailingSpaces(context.message(), LegacyComponentSerializer.legacySection().serialize(build(context)));
    }

    public JsonElement buildJson(MessageContext context) {
        return GsonComponentSerializer.gson().serializeToTree(build(context));
    }

    public Optional<String> legacyFormat(@NonNull FPlayer fPlayer, @NonNull String message) {
        LegacyComponentSerializer legacyComponentSerializer = LegacyComponentSerializer.legacySection();

        try {
            Component deserialized = legacyComponentSerializer.deserialize(message);

            MessageContext context = createContext(fPlayer, Strings.CS.replace(message, "§", "&"))
                    .addFlag(MessageFlag.USER_MESSAGE, true);

            Component component = build(context)
                    .applyFallbackStyle(deserialized.style())
                    .mergeStyle(deserialized);

            String formattedMessage = LegacyComponentSerializer.legacySection().serialize(component);
            if (!message.equalsIgnoreCase(formattedMessage)) {
                return Optional.of(formattedMessage);
            }

        } catch (Exception ignored) {
            // ignore problem
        }

        return Optional.empty();
    }

    public String addTrailingSpaces(String rawString, String finalString) {
        if (StringUtils.isEmpty(rawString)) return finalString;

        int countRawSpaces = countTrailingSpaces(rawString);
        if (countRawSpaces == 0) return finalString;

        int countFinalSpaces = countTrailingSpaces(finalString);

        if (countRawSpaces > countFinalSpaces) {
            finalString = finalString + " ".repeat(countRawSpaces - countFinalSpaces);
        }

        return finalString;
    }

    public int countTrailingSpaces(String string) {
        if (StringUtils.isEmpty(string)) return 0;

        int count = 0;
        for (int i = string.length() - 1; i >= 0; i--) {
            if (string.charAt(i) != ' ') {
                break;
            }

            count++;
        }

        return count;
    }

    public enum ReplacementTag {
        AFK_SUFFIX,
        ANIMATION,
        MUTE_SUFFIX,
        STREAM_PREFIX,
        VAULT_SUFFIX,
        VAULT_PREFIX,
        DELETE,
        DISPLAY_NAME,
        PLAYER,
        NICKNAME,
        CONSTANT,
        REPLACEMENT,
        MENTION,
        SWEAR,
        QUESTION,
        TRANSLATE,
        WORLD_PREFIX,
        PLAYER_HEAD,
        PLAYER_HEAD_OR,
        SPRITE,
        SPRITE_OR,
        FCOLOR;

        @Subst("")
        public String getTagName() {
            return name().toLowerCase();
        }

        public TagResolver emptyResolver() {
            return ReplacementTag.emptyResolver(getTagName());
        }

        public static TagResolver emptyResolver(@TagPattern String tag) {
            return TagResolver.resolver(tag, (argumentQueue, context) ->
                    Tag.selfClosingInserting(Component.empty())
            );
        }

        public static Tag emptyTag() {
            return Tag.selfClosingInserting(Component.empty());
        }

    }
}