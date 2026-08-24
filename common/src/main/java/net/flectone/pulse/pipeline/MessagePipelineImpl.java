package net.flectone.pulse.pipeline;

import com.github.benmanes.caffeine.cache.Cache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.constant.MessageFlag;
import net.flectone.pulse.dispatcher.EventDispatcher;
import net.flectone.pulse.logging.FLogger;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.serializer.ComponentSerializer;
import net.flectone.pulse.util.tag.CachingTagResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.TagPattern;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MessagePipelineImpl implements MessagePipeline {

    private final FLogger fLogger;
    private final MiniMessage miniMessage;
    private final EventDispatcher eventDispatcher;
    private final ComponentSerializer componentSerializer;
    private final @Named("messageContext") Cache<MessageContext.CacheKey, Component> messageContextCache;

    @NonNull
    @Override
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
    @Override
    public String buildPlain(MessageContext messageContext) {
        return componentSerializer.toPlain(build(messageContext));
    }

    @NonNull
    @Override
    public String buildLegacy(MessageContext messageContext) {
        return componentSerializer.toLegacy(build(messageContext));
    }

    @Override
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
    @Override
    public String buildJson(MessageContext messageContext) {
        return componentSerializer.toJson(build(messageContext));
    }

    @NonNull
    @Override
    public Component build(MessageContext messageContext) {
        // no need to build empty message
        if (StringUtils.isEmpty(messageContext.message())) return Component.empty();

        // if a message is created very often, it can be retrieved from the cache
        // because it is only 1 second and context of placeholders will be correct
        MessageContext.CacheKey messageContextCacheKey = messageContext.createCacheKey();
        Component cachedComponent = messageContextCache.getIfPresent(messageContextCacheKey);
        if (cachedComponent != null) return cachedComponent;

        // update context
        messageContext = eventDispatcher.dispatch(new MessageFormattingEvent(messageContext)).context();

        Component component = deserialize(messageContext);

        if (messageContext.isFlag(MessageFlag.USE_CACHE)) {
            messageContextCache.put(messageContextCacheKey, component);
        }

        return component;
    }

    @Override
    public @NonNull String parse(MessageContext messageContext) {
        String message = messageContext.message();
        if (StringUtils.isEmpty(message)) return "";

        return eventDispatcher.dispatch(new MessageFormattingEvent(messageContext)).context().message();
    }

    @NonNull
    private Component deserialize(MessageContext messageContext) {
        if (messageContext.isFlag(MessageFlag.REMOVE_DISABLED_TAGS) && !messageContext.isFlag(MessageFlag.PLAYER_MESSAGE)) {
            messageContext = messageContext.addTagResolver(ReplacementTag.emptyResolver(messageContext.tagResolver()));
        }

        try {
            return miniMessage.deserialize(
                    // always need to replace legacy § with & to avoid MiniMessage problems
                    Strings.CS.replace(messageContext.message(), "§", "&"),
                    messageContext.tagResolver()
            );
        } catch (Exception e) {
            fLogger.warning(e);
        }

        return Component.empty();
    }

    @Override
    public TagResolver messageTag(Component message) {
        return resolver("message", (_, _) -> Tag.inserting(message));
    }

    @Override
    public TagResolver messageTag(FEntity sender, FPlayer receiver, String message) {
        return messageTag(messageComponent(sender, receiver, message));
    }

    @Override
    public Component messageComponent(FEntity sender, FPlayer receiver, String message) {
        return build(MessageContext.builder()
                .sender(sender)
                .receiver(receiver)
                .message(message)
                .flag(MessageFlag.PLAYER_MESSAGE, true)
                .build()
        );
    }

    @Override
    public TagResolver targetTag(@TagPattern String tag, String formatTarget, FPlayer receiver, @Nullable FEntity target) {
        if (target == null) return ReplacementTag.emptyResolver(tag);

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

    @Override
    public TagResolver targetTag(@TagPattern String tag, FPlayer receiver, @Nullable FEntity target) {
        return targetTag(tag, "<display_name:<index>>", receiver, target);
    }

    @Override
    public TagResolver targetTag(FPlayer receiver, @Nullable FEntity target) {
        return targetTag("target", receiver, target);
    }

    @Override
    public @NonNull TagResolver resolver(@TagPattern @NonNull String name, @NonNull BiFunction<ArgumentQueue, Context, Tag> handler) {
        return resolver(Set.of(name), handler);
    }

    @Override
    public @NonNull TagResolver resolver(@TagPattern @NonNull String name, @NonNull Tag tag) {
        return resolver(name, (_, _) -> tag);
    }

    @Override
    public @NonNull TagResolver resolver(@TagPattern @NonNull String name, @NonNull Component component) {
        return resolver(name, (_, _) -> Tag.selfClosingInserting(component));
    }

    @Override
    public @NonNull TagResolver resolver(@NonNull Set<String> names, @NonNull Tag tag) {
        return resolver(names, (_, _) -> tag);
    }

    @Override
    public @NonNull TagResolver resolver(@NonNull Set<String> names, @NonNull Component component) {
        return resolver(names, (_, _) -> Tag.selfClosingInserting(component));
    }

    @Override
    public @NonNull TagResolver resolver(@NonNull Set<String> names, @NonNull BiFunction<ArgumentQueue, Context, Tag> handler) {
        return new CachingTagResolver(names, handler, fLogger);
    }

}
