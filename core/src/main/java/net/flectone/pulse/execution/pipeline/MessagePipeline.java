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
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MessagePipeline {

    private final FLogger fLogger;
    private final MiniMessage miniMessage;
    private final EventDispatcher eventDispatcher;

    public Builder builder(@NotNull String message) {
        return builder(FPlayer.UNKNOWN, message);
    }

    public Builder builder(@NotNull FPlayer sender, @NotNull String message) {
        return new Builder(sender, sender, message, this);
    }

    public Builder builder(@NotNull FEntity sender, @NotNull FPlayer receiver, @NotNull String message) {
        return new Builder(sender, receiver, message, this);
    }

    public Builder builder(UUID messageUUID, @NotNull FEntity sender, @NotNull FPlayer receiver, @NotNull String message) {
        return new Builder(messageUUID, sender, receiver, message, this);
    }

    public Optional<String> legacyFormat(@NotNull FPlayer fPlayer, @NotNull String message) {
        LegacyComponentSerializer legacyComponentSerializer = LegacyComponentSerializer.legacySection();

        try {
            Component deserialized = legacyComponentSerializer.deserialize(message);

            Component component = builder(fPlayer, Strings.CS.replace(message, "§", "&"))
                    .flag(MessageFlag.USER_MESSAGE, true)
                    .build()
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

    // MiniMessage removes trailing spaces during serialization, so we need to add them back
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

    public record Builder(
            MessageContext context,
            MessagePipeline pipeline
    ) {

        public Builder(UUID messageUUID, FEntity sender, FPlayer receiver, String message, MessagePipeline pipeline) {
            this(new MessageContext(messageUUID, sender, receiver, message), pipeline);
        }

        public Builder(FEntity sender, FPlayer receiver, String message, MessagePipeline pipeline) {
            this(UUID.randomUUID(), sender, receiver, message, pipeline);
        }

        @CheckReturnValue
        public Builder flag(MessageFlag flag, boolean value) {
            return new Builder(context.withFlag(flag, value), pipeline);
        }

        @CheckReturnValue
        public Builder flags(Map<MessageFlag, Boolean> flags) {
            return new Builder(context.withFlags(flags), pipeline);
        }

        @CheckReturnValue
        public Builder userMessage(String userMessage) {
            return new Builder(context.withUserMessage(userMessage), pipeline);
        }

        @CheckReturnValue
        public Builder translate(boolean translate) {
            return flag(MessageFlag.TRANSLATE, translate);
        }

        @CheckReturnValue
        public Builder tagResolvers(TagResolver... tagResolvers) {
            return new Builder(context.addTagResolvers(tagResolvers), pipeline);
        }

        public Component build() {
            MessageFormattingEvent event = pipeline.eventDispatcher.dispatch(new MessageFormattingEvent(context));
            MessageContext eventContext = event.context();

            MessageContext finalContext = eventContext;

            if (finalContext.isFlag(MessageFlag.REPLACE_DISABLED_TAGS) && !finalContext.isFlag(MessageFlag.USER_MESSAGE)) {
                finalContext = finalContext.addTagResolvers(
                        Arrays.stream(ReplacementTag.values())
                        .filter(tag -> eventContext.tagResolvers()
                                .stream()
                                .filter(tagResolver -> !tagResolver.equals(StandardTags.translatable()))
                                .noneMatch(tagResolver -> tagResolver.has(tag.getTagName()))
                        )
                        .map(ReplacementTag::empty)
                        .toList()
                );
            }

            try {
                return pipeline.miniMessage.deserialize(
                        Strings.CS.replace(finalContext.message(), "§", "&"),
                        finalContext.tagResolvers().toArray(new TagResolver[0])
                );
            } catch (Exception e) {
                pipeline.fLogger.warning(e);
            }

            return Component.empty();
        }

        public String defaultSerializerBuild() {
            return pipeline.addTrailingSpaces(context.message(), MiniMessage.miniMessage().serialize(build()));
        }

        public String plainSerializerBuild() {
            return pipeline.addTrailingSpaces(context.message(), PlainTextComponentSerializer.plainText().serialize(build()));
        }

        public String legacySerializerBuild() {
            return pipeline.addTrailingSpaces(context.message(), LegacyComponentSerializer.legacySection().serialize(build()));
        }

        public JsonElement jsonSerializerBuild() {
            return GsonComponentSerializer.gson().serializeToTree(build());
        }

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