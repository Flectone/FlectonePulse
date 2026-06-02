package net.flectone.pulse.model.event.message.context;

import lombok.Builder;
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

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

@With
@Builder(toBuilder = true)
public record MessageContext(
        @NonNull Map<MessageFlag, Boolean> flags,
        @NonNull TagResolver tagResolver,
        @NonNull FEntity sender,
        @NonNull FPlayer receiver,
        @NonNull UUID messageUUID,
        @NonNull String message,
        @Nullable String userMessage
) {

    public MessageContext {
        if (sender == null) sender = FPlayer.UNKNOWN;
        if (receiver == null) receiver = sender instanceof FPlayer fPlayer ? fPlayer : FPlayer.UNKNOWN;
        if (messageUUID == null) messageUUID = UUID.randomUUID();

        flags = Map.copyOf(new EnumMap<>(flags != null && !flags.isEmpty() ? flags : new EnumMap<>(MessageFlag.class)));
        tagResolver = tagResolver == null ? TagResolver.builder().build() : tagResolver;
        userMessage = StringUtils.defaultString(userMessage);
    }

    public static class MessageContextBuilder {

        public MessageContextBuilder flags(@NonNull Map<MessageFlag, Boolean> flags) {
            this.flags = Map.copyOf(flags);
            return this;
        }

        public MessageContextBuilder flags(MessageFlag @NonNull [] flags, boolean @NonNull [] values) {
            if (ArrayUtils.isEmpty(flags) || ArrayUtils.isEmpty(values)) return this;
            if (flags.length != values.length) {
                throw new IllegalArgumentException("Flag and Value array lengths don't match: " + flags.length + " vs " + values.length);
            }

            if (this.flags == null || this.flags.isEmpty()) {
                this.flags = new EnumMap<>(MessageFlag.class);
            } else {
                this.flags = new EnumMap<>(this.flags);
            }

            for (int i = 0; i < flags.length; i++) {
                this.flags.put(flags[i], values[i]);
            }

            return this;
        }

        public MessageContextBuilder flag(@NonNull MessageFlag flag, boolean value) {
            if (this.flags == null || this.flags.isEmpty()) {
                this.flags = new EnumMap<>(MessageFlag.class);
            } else {
                this.flags = new EnumMap<>(this.flags);
            }

            this.flags.put(flag, value);
            return this;
        }

        public MessageContextBuilder tagResolver(@Nullable TagResolver tagResolver) {
            if (tagResolver == null) return this;

            if (this.tagResolver == null) {
                this.tagResolver = tagResolver;
            } else {
                this.tagResolver = TagResolver.resolver(this.tagResolver, tagResolver);
            }

            return this;
        }

        public MessageContextBuilder tagResolvers(@NonNull TagResolver... resolvers) {
            if (ArrayUtils.isEmpty(resolvers)) return this;

            if (this.tagResolver == null) {
                this.tagResolver = TagResolver.resolver(resolvers);
            } else {
                this.tagResolver = TagResolver.resolver(this.tagResolver, TagResolver.resolver(resolvers));
            }

            return this;
        }

    }

    @CheckReturnValue
    public MessageContext addFlag(@NonNull MessageFlag flag, boolean value) {
        Map<MessageFlag, Boolean> newFlags = this.flags.isEmpty()
                ? new EnumMap<>(MessageFlag.class)
                : new EnumMap<>(this.flags);

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

        Map<MessageFlag, Boolean> newFlags = this.flags.isEmpty()
                ? new EnumMap<>(MessageFlag.class)
                : new EnumMap<>(this.flags);

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
    public MessageContext addTagResolvers(@NonNull TagResolver... resolvers) {
        if (resolvers == null || resolvers.length == 0) return this;

        return withTagResolver(TagResolver.resolver(this.tagResolver, TagResolver.resolver(resolvers)));
    }

    public boolean isFlag(MessageFlag flag) {
        return flags.getOrDefault(flag, flag.getDefaultValue());
    }

}