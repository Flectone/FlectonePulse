package net.flectone.pulse.processing.context;

import lombok.With;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.util.constant.MessageFlag;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.CheckReturnValue;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@With
public record MessageContext(
        Map<MessageFlag, Boolean> flags,
        Set<TagResolver> tagResolvers,
        FEntity sender,
        FPlayer receiver,
        UUID messageUUID,
        String message,
        String userMessage
) {

    public MessageContext {
        flags = Map.copyOf(new EnumMap<>(flags != null && !flags.isEmpty() ? flags : new EnumMap<>(MessageFlag.class)));
        tagResolvers = Set.copyOf(tagResolvers != null ? tagResolvers : Set.of());
        userMessage = StringUtils.defaultString(userMessage);
    }

    public MessageContext(UUID messageUUID, FEntity sender, FPlayer receiver, String message) {
        this(
                new EnumMap<>(MessageFlag.class),
                Set.of(),
                sender,
                receiver,
                messageUUID,
                message,
                null
        );
    }

    @CheckReturnValue
    public MessageContext withFlag(MessageFlag flag, boolean value) {
        Map<MessageFlag, Boolean> newFlags = this.flags.isEmpty()
                ? new EnumMap<>(MessageFlag.class)
                : new EnumMap<>(this.flags);

        newFlags.put(flag, value);
        return withFlags(newFlags);
    }

    @CheckReturnValue
    public MessageContext addTagResolver(TagResolver tagResolver) {
        Set<TagResolver> newResolvers = new HashSet<>(this.tagResolvers);
        newResolvers.add(tagResolver);

        return withTagResolvers(newResolvers);
    }

    @CheckReturnValue
    public MessageContext addTagResolvers(Collection<TagResolver> tagResolvers) {
        if (tagResolvers == null || tagResolvers.isEmpty()) return this;

        Set<TagResolver> newResolvers = new HashSet<>(this.tagResolvers);
        newResolvers.addAll(tagResolvers);

        return withTagResolvers(newResolvers);
    }

    @CheckReturnValue
    public MessageContext addTagResolvers(TagResolver... resolvers) {
        if (resolvers == null || resolvers.length == 0) return this;

        return addTagResolvers(Arrays.asList(resolvers));
    }

    @CheckReturnValue
    public MessageContext addTagResolver(MessagePipeline.ReplacementTag replacementTag,
                                         BiFunction<ArgumentQueue, Context, Tag> handler) {
        return addTagResolver(TagResolver.resolver(replacementTag.getTagName(), handler));
    }

    @CheckReturnValue
    public MessageContext addTagResolver(Set<MessagePipeline.ReplacementTag> replacementTags,
                                         BiFunction<ArgumentQueue, Context, Tag> handler) {
        Set<String> tags = replacementTags.stream()
                .map(MessagePipeline.ReplacementTag::getTagName)
                .collect(Collectors.toSet());

        return addTagResolver(TagResolver.resolver(tags, handler));
    }

    public boolean isFlag(MessageFlag flag) {
        return flags.getOrDefault(flag, flag.getDefaultValue());
    }

}