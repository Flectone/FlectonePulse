package net.flectone.pulse.execution.pipeline;

import com.google.gson.Gson;
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
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.intellij.lang.annotations.Subst;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static net.flectone.pulse.execution.pipeline.MessagePipeline.ReplacementTag.emptyResolver;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MessagePipeline {

    private final FLogger fLogger;
    private final MiniMessage miniMessage;
    private final EventDispatcher eventDispatcher;
    private final Gson gson;

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

        if (eventContext.isFlag(MessageFlag.REMOVE_DISABLED_TAGS) && !eventContext.isFlag(MessageFlag.PLAYER_MESSAGE)) {
            TagResolver tagResolver = eventContext.tagResolver();
            eventContext = eventContext.addTagResolvers(Arrays.stream(ReplacementTag.values())
                    .filter(tag -> !tagResolver.has(tag.getTagName()))
                    .map(ReplacementTag::emptyResolver)
                    .toList()
            );
        }

        try {
            return miniMessage.deserialize(
                    // always need to replace legacy § with & to avoid MiniMessage problems
                    Strings.CS.replace(eventContext.message(), "§", "&"),
                    eventContext.tagResolver()
            );
        } catch (Exception e) {
            fLogger.warning(e);
        }

        return Component.empty();
    }

    public String buildDefault(MessageContext context) {
        // add a space so that MiniMessage correctly deserializes closed tags
        // https://github.com/Flectone/FlectonePulse/issues/243
        context = context.withMessage(context.message() + " ");

        // build and serialize component
        String serializedComponent = MiniMessage.miniMessage().serialize(build(context));

        // remove last space
        return StringUtils.chop(serializedComponent);
    }

    public String buildPlain(MessageContext context) {
        return PlainTextComponentSerializer.plainText().serialize(build(context));
    }

    public String buildLegacy(MessageContext context) {
        return LegacyComponentSerializer.legacySection().serialize(build(context));
    }

    public JsonElement buildJson(MessageContext context) {
        return gson.toJsonTree(build(context));
    }

    public String buildJsonString(MessageContext context) {
        return gson.toJson(buildJson(context));
    }

    public Optional<String> legacyFormat(@NonNull FPlayer fPlayer, @NonNull String message) {
        LegacyComponentSerializer legacyComponentSerializer = LegacyComponentSerializer.legacySection();

        try {
            Component deserialized = legacyComponentSerializer.deserialize(message);

            MessageContext context = createContext(fPlayer, Strings.CS.replace(message, "§", "&"))
                    .addFlag(MessageFlag.PLAYER_MESSAGE, true);

            Component component = build(context)
                    .applyFallbackStyle(deserialized.style())
                    .mergeStyle(deserialized);

            String formattedMessage = LegacyComponentSerializer.legacySection().serialize(component);
            if (!message.equalsIgnoreCase(formattedMessage)) {
                return Optional.of(formattedMessage);
            }

        } catch (Exception _) {
            // ignore problem
        }

        return Optional.empty();
    }

    public TagResolver messageTag(Component message) {
        return TagResolver.resolver("message", (_, _) -> Tag.inserting(message));
    }

    public TagResolver targetTag(@TagPattern String tag, String formatTarget, FPlayer receiver, @Nullable FEntity target) {
        if (target == null) return emptyResolver(tag);

        return TagResolver.resolver(tag, (argumentQueue, _) -> {
            int targetIndex = 0;
            if (argumentQueue.hasNext()) {
                targetIndex = argumentQueue.pop().asInt().orElse(0);
            }

            MessageContext messageContext = createContext(target, receiver,
                    Strings.CS.replace(formatTarget, "<index>", String.valueOf(targetIndex))
            );

            return Tag.selfClosingInserting(build(messageContext));
        });
    }

    public TagResolver targetTag(@TagPattern String tag, FPlayer receiver, @Nullable FEntity target) {
        return targetTag(tag, "<display_name:<index>>", receiver, target);
    }

    public TagResolver targetTag(FPlayer receiver, @Nullable FEntity target) {
        return targetTag("target", receiver, target);
    }

    public enum ReplacementTag {
        AFK,
        ANIMATION,
        CONDITION,
        MUTE,
        STREAM,
        SUFFIX,
        PREFIX,
        DELETE,
        DISPLAY_NAME,
        PLAYER,
        NICKNAME,
        CONSTANT,
        REPLACEMENT,
        MENTION,
        ONLINE,
        SWEAR,
        QUESTION,
        TRANSLATION,
        WORLD,
        PLAYER_HEAD,
        PLAYER_HEAD_OR,
        SPRITE,
        SPRITE_OR,
        TEXTURE,
        TEXTURE_OR,
        FCOLOR;

        public static TagResolver emptyResolver(@TagPattern String tag) {
            return TagResolver.resolver(tag, (_, _) ->
                    Tag.selfClosingInserting(Component.empty())
            );
        }

        public static Tag emptyTag() {
            return Tag.selfClosingInserting(Component.empty());
        }

        @Subst("")
        public String getTagName() {
            return name().toLowerCase();
        }

        public TagResolver emptyResolver() {
            return emptyResolver(getTagName());
        }

    }
}