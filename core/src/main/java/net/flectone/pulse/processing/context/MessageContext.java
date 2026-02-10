package net.flectone.pulse.processing.context;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.With;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.util.constant.MessageFlag;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.CheckReturnValue;
import org.jspecify.annotations.NonNull;

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
        tagResolvers = tagResolvers == null ? Collections.emptySet() : new ObjectOpenHashSet<>(tagResolvers);
        userMessage = StringUtils.defaultString(userMessage);
    }

    public MessageContext(UUID messageUUID, FEntity sender, FPlayer receiver, String message) {
        this(new EnumMap<>(MessageFlag.class), Collections.emptySet(), sender, receiver, messageUUID, message, null);
    }

    @CheckReturnValue
    public MessageContext addFlag(MessageFlag flag, boolean value) {
        Map<MessageFlag, Boolean> newFlags = newMutableFlags();

        newFlags.put(flag, value);

        return withFlags(newFlags);
    }

    @CheckReturnValue
    public MessageContext addFlags(MessageFlag @NonNull [] flags, boolean @NonNull [] values) {
        if (ArrayUtils.isEmpty(flags) || ArrayUtils.isEmpty(values)) return this;

        int flagsLength = flags.length;
        int valuesLength = values.length;

        if (flagsLength != valuesLength) {
            throw new IllegalArgumentException("Flag and Value array lengths don't match: " + flagsLength + " vs " + valuesLength);
        }

        Map<MessageFlag, Boolean> newFlags = newMutableFlags();

        for (int i = 0; i < flagsLength; i++) {
            newFlags.put(flags[i], values[i]);
        }

        return withFlags(newFlags);
    }

    @CheckReturnValue
    public MessageContext addTagResolver(TagResolver tagResolver) {
        Set<TagResolver> newResolvers = new ObjectOpenHashSet<>(this.tagResolvers);
        newResolvers.add(tagResolver);

        return withTagResolvers(newResolvers);
    }

    @CheckReturnValue
    public MessageContext addTagResolvers(Collection<TagResolver> tagResolvers) {
        if (tagResolvers == null || tagResolvers.isEmpty()) return this;

        Set<TagResolver> newResolvers = new ObjectOpenHashSet<>(this.tagResolvers);
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

    public Map<MessageFlag, Boolean> newMutableFlags() {
        return this.flags.isEmpty()
                ? new EnumMap<>(MessageFlag.class)
                : new EnumMap<>(this.flags);
    }

}