package net.flectone.pulse.model.event.message.context;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.util.constant.MessageFlag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.ArrayUtils;
import org.jspecify.annotations.NonNull;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

@With
@Builder(toBuilder = true)
record MessageContextImpl(
        @NonNull Map<MessageFlag, Boolean> flags,
        @NonNull TagResolver tagResolver,
        @NonNull FEntity sender,
        @NonNull FPlayer receiver,
        @NonNull UUID uuid,
        @NonNull String message
) implements MessageContext {

    public static class MessageContextImplBuilder {

        public MessageContextImplBuilder flags(@NonNull Map<MessageFlag, Boolean> flags) {
            this.flags = Map.copyOf(flags);
            return this;
        }

        public MessageContextImplBuilder flags(MessageFlag @NonNull [] flags, boolean @NonNull [] values) {
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

        public MessageContextImplBuilder flag(@NonNull MessageFlag flag, boolean value) {
            if (this.flags == null || this.flags.isEmpty()) {
                this.flags = new EnumMap<>(MessageFlag.class);
            } else {
                this.flags = new EnumMap<>(this.flags);
            }

            this.flags.put(flag, value);
            return this;
        }

        public MessageContextImplBuilder tagResolver(@org.jspecify.annotations.Nullable TagResolver tagResolver) {
            if (tagResolver == null) return this;

            if (this.tagResolver == null) {
                this.tagResolver = tagResolver;
            } else {
                this.tagResolver = TagResolver.resolver(this.tagResolver, tagResolver);
            }

            return this;
        }

        public MessageContextImplBuilder tagResolvers(@NonNull TagResolver... resolvers) {
            if (ArrayUtils.isEmpty(resolvers)) return this;

            if (this.tagResolver == null) {
                this.tagResolver = TagResolver.resolver(resolvers);
            } else {
                this.tagResolver = TagResolver.resolver(this.tagResolver, TagResolver.resolver(resolvers));
            }

            return this;
        }
    }

    public MessageContextImpl {
        if (sender == null) sender = FPlayer.UNKNOWN;
        if (receiver == null) receiver = sender instanceof FPlayer fPlayer ? fPlayer : FPlayer.UNKNOWN;
        if (uuid == null) uuid = UUID.randomUUID();

        flags = Map.copyOf(new EnumMap<>(flags != null && !flags.isEmpty() ? flags : new EnumMap<>(MessageFlag.class)));
        tagResolver = tagResolver == null ? TagResolver.builder().build() : tagResolver;
    }

    @Override
    public MessageContextImpl base() {
        return this;
    }

    @Override
    public CacheKeyImpl createCacheKey() {
        return new CacheKeyImpl(flags, sender.uuid(), receiver.uuid(), message);
    }

    public record CacheKeyImpl(
            @NonNull Map<MessageFlag, Boolean> flags,
            @NonNull UUID sender,
            @NonNull UUID receiver,
            @NonNull String message
    ) implements CacheKey {

        @Override
        public CacheKeyImpl base() {
            return this;
        }

    }

}