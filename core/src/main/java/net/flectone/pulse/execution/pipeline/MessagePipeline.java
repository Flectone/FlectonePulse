package net.flectone.pulse.execution.pipeline;

import com.google.gson.JsonElement;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
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
import org.apache.commons.lang3.Strings;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
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
        return new Builder(sender, sender, message);
    }

    public Builder builder(@NotNull FEntity sender, @NotNull FPlayer receiver, @NotNull String message) {
        return new Builder(sender, receiver, message);
    }

    public Builder builder(UUID messageUUID, @NotNull FEntity sender, @NotNull FPlayer receiver, @NotNull String message) {
        return new Builder(messageUUID, sender, receiver, message);
    }

    public class Builder {

        @Getter private final MessageContext context;

        public Builder(UUID messageUUID, FEntity sender, FPlayer receiver, String message) {
            this.context = new MessageContext(messageUUID, sender, receiver, message);
        }

        public Builder(FEntity sender, FPlayer receiver, String message) {
            this(UUID.randomUUID(), sender, receiver, message);
        }

        public Builder flag(MessageFlag flag, boolean value) {
            context.setFlag(flag, value);
            return this;
        }

        public Builder flags(Map<MessageFlag, Boolean> flags) {
            context.setFlags(flags);
            return this;
        }

        public Builder setUserMessage(String userMessage) {
            context.setUserMessage(userMessage);
            return this;
        }

        public Builder translate(boolean translate) {
            context.setFlag(MessageFlag.TRANSLATE, translate);
            return this;
        }

        public Builder tagResolvers(TagResolver... tagResolvers) {
            context.addTagResolvers(tagResolvers);
            return this;
        }

        public Component build() {
            eventDispatcher.dispatch(new MessageFormattingEvent(context));

            // replace disabled tags
            if (context.isFlag(MessageFlag.REPLACE_DISABLED_TAGS) && !context.isFlag(MessageFlag.USER_MESSAGE)) {
                Arrays.stream(ReplacementTag.values())
                        .filter(tag -> context.getTagResolvers()
                                .stream()
                                .filter(tagResolver -> !tagResolver.equals(StandardTags.translatable()))
                                .noneMatch(tagResolver -> tagResolver.has(tag.getTagName()))
                        )
                        .forEach(tag -> context.addReplacementTag(tag.empty()));
            }

            try {
                return miniMessage.deserialize(
                        Strings.CS.replace(context.getMessage(), "§", "&"),
                        context.getTagResolvers().toArray(new TagResolver[0])
                );
            } catch (Exception e) {
                fLogger.warning(e);
            }

            return Component.empty();
        }

        public String defaultSerializerBuild() {
            return MiniMessage.miniMessage().serialize(build());
        }

        public String plainSerializerBuild() {
            return PlainTextComponentSerializer.plainText().serialize(build());
        }

        public String legacySerializerBuild() {
            return LegacyComponentSerializer.legacySection().serialize(build());
        }

        public JsonElement jsonSerializerBuild() {
            return GsonComponentSerializer.gson().serializeToTree(build());
        }
    }

    public enum ReplacementTag {

        AFK_SUFFIX,
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

        @Deprecated
        IMAGE,

        @Deprecated
        PING,

        @Deprecated
        TPS,

        @Deprecated
        ONLINE,

        @Deprecated
        COORDS,

        @Deprecated
        STATS,

        @Deprecated
        STYLE,

        @Deprecated
        SKIN,

        @Deprecated
        ITEM,

        @Deprecated
        URL,

        @Deprecated
        FCOLOR,

        @Deprecated
        TRANSLATETO,

        @Deprecated
        SPOILER;

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
