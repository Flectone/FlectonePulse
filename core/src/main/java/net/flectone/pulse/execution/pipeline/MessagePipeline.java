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
import net.flectone.pulse.processing.serializer.ComponentSerializer;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.TagPattern;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.intellij.lang.annotations.Subst;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;

import static net.flectone.pulse.execution.pipeline.MessagePipeline.ReplacementTag.emptyResolver;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MessagePipeline {

    private final FLogger fLogger;
    private final MiniMessage miniMessage;
    private final EventDispatcher eventDispatcher;
    private final ComponentSerializer componentSerializer;

    @NonNull
    public String buildStandard(MessageContext messageContext) {
        // add a space so that MiniMessage correctly deserializes closed tags
        // https://github.com/Flectone/FlectonePulse/issues/243
        messageContext = messageContext.withMessage(messageContext.message() + " ");

        // build and serialize component
        String serializedComponent = componentSerializer.toStandard(build(messageContext));

        // remove last space
        return Strings.CS.removeEnd(serializedComponent, " ");
    }

    @NonNull
    public String buildPlain(MessageContext messageContext) {
        return componentSerializer.toPlain(build(messageContext));
    }

    @NonNull
    public String buildLegacy(MessageContext messageContext) {
        return componentSerializer.toLegacy(build(messageContext));
    }

    public Optional<String> buildLegacy(@NonNull FPlayer fPlayer, @NonNull String message) {
        try {
            Component deserialized = componentSerializer.fromLegacy(message);

            MessageContext messageContext = MessageContext.builder()
                    .sender(fPlayer)
                    .message(Strings.CS.replace(message, "§", "&"))
                    .flags(
                            new MessageFlag[]{MessageFlag.PLAYER_MESSAGE, MessageFlag.OBJECT_DEFAULT_VALUE},
                            new boolean[]{true, true}
                    )
                    .build();

            Component component = build(messageContext)
                    .applyFallbackStyle(deserialized.style())
                    .mergeStyle(deserialized);

            String formattedMessage = componentSerializer.toLegacy(component);
            if (!message.equalsIgnoreCase(formattedMessage)) {
                return Optional.of(formattedMessage);
            }

        } catch (Exception _) {
            // ignore problem
        }

        return Optional.empty();
    }

    @NonNull
    public String buildJson(MessageContext messageContext) {
        return componentSerializer.toJson(build(messageContext));
    }

    @NonNull
    public JsonElement buildJsonTree(MessageContext messageContext) {
        return componentSerializer.toJsonTree(build(messageContext));
    }

    @NonNull
    public Component build(MessageContext messageContext) {
        // no need to build empty message
        if (StringUtils.isEmpty(messageContext.message())) return Component.empty();

        MessageFormattingEvent event = eventDispatcher.dispatch(new MessageFormattingEvent(messageContext));
        MessageContext eventContext = event.context();

        if (eventContext.isFlag(MessageFlag.REMOVE_DISABLED_TAGS) && !eventContext.isFlag(MessageFlag.PLAYER_MESSAGE)) {
            TagResolver tagResolver = eventContext.tagResolver();
            eventContext = eventContext.addTagResolvers(Arrays.stream(ReplacementTag.values())
                    .filter(tag -> !tagResolver.has(tag.getTagName()))
                    .map(ReplacementTag::emptyResolver)
                    .toArray(TagResolver[]::new)
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

    public TagResolver messageTag(Component message) {
        return resolver("message", (_, _) -> Tag.inserting(message));
    }

    public TagResolver targetTag(@TagPattern String tag, String formatTarget, FPlayer receiver, @Nullable FEntity target) {
        if (target == null) return emptyResolver(tag);

        return resolver(tag, (argumentQueue, _) -> {
            int targetIndex = 0;
            if (argumentQueue.hasNext()) {
                targetIndex = argumentQueue.pop().asInt().orElse(0);
            }

            MessageContext messageContext = MessageContext.builder()
                    .sender(target)
                    .receiver(receiver)
                    .message(Strings.CS.replace(formatTarget, "<index>", String.valueOf(targetIndex)))
                    .build();

            return Tag.selfClosingInserting(build(messageContext));
        });
    }

    public TagResolver targetTag(@TagPattern String tag, FPlayer receiver, @Nullable FEntity target) {
        return targetTag(tag, "<display_name:<index>>", receiver, target);
    }

    public TagResolver targetTag(FPlayer receiver, @Nullable FEntity target) {
        return targetTag("target", receiver, target);
    }

    public @NonNull TagResolver resolver(@TagPattern @NonNull String name, @NonNull BiFunction<ArgumentQueue, Context, Tag> handler) {
        return resolver(Set.of(name), handler);
    }

    public @NonNull TagResolver resolver(@TagPattern @NonNull String name, @NonNull Tag tag) {
        return resolver(name, (_, _) -> tag);
    }

    public @NonNull TagResolver resolver(@TagPattern @NonNull String name, @NonNull Component component) {
        return resolver(name, (_, _) -> Tag.selfClosingInserting(component));
    }

    public @NonNull TagResolver resolver(@NonNull Set<String> names, @NonNull Tag tag) {
        return resolver(names, (_, _) -> tag);
    }

    public @NonNull TagResolver resolver(@NonNull Set<String> names, @NonNull Component component) {
        return resolver(names, (_, _) -> Tag.selfClosingInserting(component));
    }

    // wait for https://github.com/PaperMC/adventure/issues/1424
    public @NonNull TagResolver resolver(@NonNull Set<String> names, @NonNull BiFunction<ArgumentQueue, Context, Tag> handler) {
        return new TagResolver() {

            private String cachedKey;
            private Tag cachedTag;
            private Map<String, Tag> cachedTags; // lazy map

            @Override
            public @Nullable Tag resolve(@NonNull String name, @NonNull ArgumentQueue arguments, @NonNull Context context) throws ParsingException {
                if (!names.contains(name)) return null;

                // build cache key from tag name + all arguments
                String key = name + ":" + arguments;

                // multiple unique keys seen, use map
                if (cachedTags != null) {
                    return cachedTags.computeIfAbsent(key, _ -> handler.apply(arguments, context));
                }

                // first call, store in fields to avoid map allocation
                if (cachedKey == null) {
                    cachedKey = key;
                    cachedTag = handler.apply(arguments, context);
                    return cachedTag;
                }

                // same key as before, return cached result
                if (cachedKey.equals(key)) return cachedTag;

                // second unique key seen, upgrade to map
                cachedTags = new HashMap<>();
                cachedTags.put(cachedKey, cachedTag);

                try {
                    // create tag
                    Tag tag = handler.apply(arguments, context);

                    // save to cache
                    cachedTags.put(key, tag);

                    // return tag
                    return tag;
                } catch (ParsingException e) {
                    fLogger.warning(e);
                }

                return null;
            }

            @Override
            public boolean has(final @NonNull String name) {
                return names.contains(name);
            }

        };
    }

    public enum ReplacementTag {
        AFK,
        ANIMATION,
        CONDITION,
        MUTE,
        STREAM,
        SERVER,
        SUFFIX,
        PREFIX,
        DELETE,
        DISPLAY_NAME,
        PLAYER,
        NICKNAME,
        CONSTANT,
        REPLACEMENT,
        MENTION,
        TOPONLINE,
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