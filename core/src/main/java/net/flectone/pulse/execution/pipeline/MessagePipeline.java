package net.flectone.pulse.execution.pipeline;

import com.google.gson.JsonElement;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.TagPattern;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MessagePipeline {

    private final FLogger fLogger;
    private final MiniMessage miniMessage;
    private final EventDispatcher eventDispatcher;

    public MessageContext createContext(@NotNull String message) {
        return createContext(FPlayer.UNKNOWN, message);
    }

    public MessageContext createContext(@NotNull FPlayer sender, @NotNull String message) {
        return createContext(sender, sender, message);
    }

    public MessageContext createContext(@NotNull FEntity sender, @NotNull FPlayer receiver) {
        return new MessageContext(UUID.randomUUID(), sender, receiver, null);
    }

    public MessageContext createContext(@NotNull FEntity sender, @NotNull FPlayer receiver, @NotNull String message) {
        return new MessageContext(UUID.randomUUID(), sender, receiver, message);
    }

    public MessageContext createContext(UUID messageUUID, @NotNull FEntity sender, @NotNull FPlayer receiver, @NotNull String message) {
        return new MessageContext(messageUUID, sender, receiver, message);
    }

    public Component build(MessageContext context) {
        MessageFormattingEvent event = eventDispatcher.dispatch(new MessageFormattingEvent(context));
        MessageContext eventContext = event.context();

        MessageContext finalContext = eventContext;

        if (finalContext.isFlag(MessageFlag.REPLACE_DISABLED_TAGS) && !finalContext.isFlag(MessageFlag.USER_MESSAGE)) {
            finalContext = finalContext.addTagResolvers(Arrays.stream(ReplacementTag.values())
                    .filter(tag -> eventContext.tagResolvers()
                            .stream()
                            .filter(tagResolver -> !tagResolver.equals(StandardTags.translatable()))
                            .noneMatch(tagResolver -> tagResolver.has(tag.getTagName()))
                    )
                    .map(ReplacementTag::empty).toList()
            );
        }

        try {
            return miniMessage.deserialize(
                    Strings.CS.replace(finalContext.message(), "§", "&"),
                    finalContext.tagResolvers().toArray(new TagResolver[0])
            );
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

    public Optional<String> legacyFormat(@NotNull FPlayer fPlayer, @NotNull String message) {
        LegacyComponentSerializer legacyComponentSerializer = LegacyComponentSerializer.legacySection();

        try {
            Component deserialized = legacyComponentSerializer.deserialize(message);

            MessageContext context = createContext(fPlayer, Strings.CS.replace(message, "§", "&"))
                    .withFlag(MessageFlag.USER_MESSAGE, true);

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
        MUTE_SUFFIX,
        STREAM_PREFIX,
        VAULT_SUFFIX,
        VAULT_PREFIX,
        DELETE,
        DISPLAY_NAME,
        PLAYER,
        CONSTANT,
        REPLACEMENT,
        MENTION,
        SWEAR,
        QUESTION,
        TRANSLATE,
        WORLD_PREFIX,
        PLAYER_HEAD,
        SPRITE,
        FCOLOR;

        @Subst("")
        public String getTagName() {
            return name().toLowerCase();
        }

        public TagResolver empty() {
            return ReplacementTag.empty(getTagName());
        }

        public static TagResolver empty(@TagPattern String tag) {
            return TagResolver.resolver(tag, (argumentQueue, context) ->
                    Tag.selfClosingInserting(Component.empty())
            );
        }
    }
}