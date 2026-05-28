package net.flectone.pulse.model.event.message.context;

import lombok.With;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.util.constant.MessageFlag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.CheckReturnValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

@With
public record MessageContext(
        Map<MessageFlag, Boolean> flags,
        TagResolver tagResolver,
        FEntity sender,
        FPlayer receiver,
        UUID messageUUID,
        String message,
        String userMessage
) {

    public MessageContext {
        flags = Map.copyOf(new EnumMap<>(flags != null && !flags.isEmpty() ? flags : new EnumMap<>(MessageFlag.class)));
        tagResolver = tagResolver == null ? TagResolver.builder().build() : tagResolver;
        userMessage = StringUtils.defaultString(userMessage);
    }

    public MessageContext(UUID messageUUID, FEntity sender, FPlayer receiver, String message) {
        this(new EnumMap<>(MessageFlag.class), null, sender, receiver, messageUUID, message, null);
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
    public MessageContext addTagResolver(@Nullable TagResolver tagResolver) {
        if (tagResolver == null) return this;

        return withTagResolver(TagResolver.resolver(this.tagResolver, tagResolver));
    }

    @CheckReturnValue
    public MessageContext addTagResolvers(@Nullable Collection<TagResolver> tagResolvers) {
        if (tagResolvers == null || tagResolvers.isEmpty()) return this;

        return withTagResolver(TagResolver.resolver(this.tagResolver, TagResolver.resolver(tagResolvers)));
    }

    @CheckReturnValue
    public MessageContext addTagResolvers(@Nullable TagResolver... resolvers) {
        if (resolvers == null || resolvers.length == 0) return this;

        return addTagResolvers(Arrays.asList(resolvers));
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