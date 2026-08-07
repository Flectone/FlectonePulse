package net.flectone.pulse.model.event.message.context;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.constant.MessageFlag;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jspecify.annotations.NonNull;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/**
 * The plain message context every other context wraps.
 *
 * @param flags the formatting switches
 * @param tagResolver the MiniMessage tags in scope
 * @param sender who sent the message
 * @param receiver who reads this copy
 * @param uuid the shared message id
 * @param message the raw text
 */
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

    /**
     * Builder that merges flags and stacks tag resolvers instead of overwriting them.
     */
    public static class MessageContextImplBuilder {

        /**
         * Replaces the flags with a copy of the given map.
         *
         * @param flags the flags
         * @return this builder
         */
        public MessageContextImplBuilder flags(@NonNull Map<MessageFlag, Boolean> flags) {
            this.flags = Map.copyOf(flags);
            return this;
        }

        /**
         * Sets several flags, keeping the ones already set.
         *
         * @param flags the flags to set
         * @param values the matching states
         * @return this builder
         * @throws IllegalArgumentException if the two arrays differ in length
         */
        public MessageContextImplBuilder flags(MessageFlag @NonNull [] flags, boolean @NonNull [] values) {
            if (flags.length == 0 || values.length == 0) return this;
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

        /**
         * Sets one flag, keeping the ones already set.
         *
         * @param flag the flag
         * @param value the state
         * @return this builder
         */
        public MessageContextImplBuilder flag(@NonNull MessageFlag flag, boolean value) {
            if (this.flags == null || this.flags.isEmpty()) {
                this.flags = new EnumMap<>(MessageFlag.class);
            } else {
                this.flags = new EnumMap<>(this.flags);
            }

            this.flags.put(flag, value);
            return this;
        }

        /**
         * Layers one more resolver on top of the current ones.
         *
         * @param tagResolver the resolver, may be null
         * @return this builder
         */
        public MessageContextImplBuilder tagResolver(@org.jspecify.annotations.Nullable TagResolver tagResolver) {
            if (tagResolver == null) return this;

            if (this.tagResolver == null) {
                this.tagResolver = tagResolver;
            } else {
                this.tagResolver = TagResolver.resolver(this.tagResolver, tagResolver);
            }

            return this;
        }

        /**
         * Layers several resolvers on top of the current ones.
         *
         * @param resolvers the resolvers
         * @return this builder
         */
        public MessageContextImplBuilder tagResolvers(@NonNull TagResolver... resolvers) {
            if (resolvers.length == 0) return this;

            if (this.tagResolver == null) {
                this.tagResolver = TagResolver.resolver(resolvers);
            } else {
                this.tagResolver = TagResolver.resolver(this.tagResolver, TagResolver.resolver(resolvers));
            }

            return this;
        }
    }

    /**
     * Fills in the defaults, an unknown sender, the sender as receiver, a fresh id and empty flags.
     */
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

    /**
     * Cache key covering the fields that change how a plain message renders.
     *
     * @param flags the formatting switches
     * @param sender the sender id
     * @param receiver the receiver id
     * @param message the raw text
     */
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