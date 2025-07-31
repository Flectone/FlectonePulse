package net.flectone.pulse.pipeline;

import com.google.gson.JsonElement;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.constant.MessageFlag;
import net.flectone.pulse.context.MessageContext;
import net.flectone.pulse.dispatcher.EventDispatcher;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
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
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

@Singleton
public class MessagePipeline {

    private final FLogger fLogger;
    private final MiniMessage miniMessage;
    private final EventDispatcher eventDispatcher;

    @Inject
    public MessagePipeline(FLogger fLogger,
                           MiniMessage miniMessage,
                           EventDispatcher eventDispatcher) {
        this.fLogger = fLogger;
        this.miniMessage = miniMessage;
        this.eventDispatcher = eventDispatcher;
    }

    public Builder builder(@NotNull String message) {
        return builder(FPlayer.UNKNOWN, message);
    }

    public Builder builder(@NotNull FPlayer sender, @NotNull String message) {
        return new Builder(sender, sender, message);
    }

    public Builder builder(@NotNull FEntity sender, @NotNull FPlayer receiver, @NotNull String message) {
        return new Builder(sender, receiver, message);
    }

    public class Builder {

        private final MessageContext context;

        public Builder(FEntity sender, FPlayer receiver, String message) {
            this.context = new MessageContext(sender, receiver, message);
        }

        public Builder flag(MessageFlag flag, boolean value) {
            context.setFlag(flag, value);
            return this;
        }

        public Builder translate(String messageToTranslate, boolean translate) {
            context.setMessageToTranslate(messageToTranslate);
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
            Arrays.stream(ReplacementTag.values())
                    .filter(tag -> context.getTagResolvers()
                            .stream()
                            .filter(tagResolver -> !tagResolver.equals(StandardTags.translatable()))
                            .noneMatch(tagResolver -> tagResolver.has(tag.getTagName()))
                    )
                    .forEach(tag -> context.addReplacementTag(tag.empty()));

            try {
                return miniMessage.deserialize(
                        context.getMessage().replace("§", "&"),
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
        PING,
        TPS,
        ONLINE,
        COORDS,
        STATS,
        SKIN,
        ITEM,
        URL,
        FCOLOR,
        EMOJI,
        IMAGE,
        MENTION,
        SWEAR,
        QUESTION,
        SPOILER,
        TRANSLATE,
        TRANSLATETO,
        WORLD_PREFIX;

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
