package net.flectone.pulse.model.event.message.context;

import net.flectone.pulse.constant.MessageFlag;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/**
 * One message on its way to one receiver, carrying the raw text, who sent it, who reads it,
 * the MiniMessage tags resolved so far and the flags that switch formatting steps on or off.
 *
 * <p>Contexts are immutable. The {@code with*} and {@code add*} methods return a modified copy,
 * and richer variants such as {@link StringMessageContext} wrap a base context to carry extra data.</p>
 * @author TheFaser
 */
public interface MessageContext {

    /**
     * Starts building a message context.
     *
     * @return a new builder
     */
    static MessageContextImpl.MessageContextImplBuilder builder() {
        return MessageContextImpl.builder();
    }

    /**
     * The plain context underneath this one. Wrapping contexts delegate their shared fields to it.
     *
     * @return the base context
     */
    MessageContext base();

    /**
     * Starts a builder pre-filled with this context's values.
     *
     * @return the pre-filled builder
     */
    default MessageContextImpl.MessageContextImplBuilder toBuilder() {
        return base().toBuilder();
    }

    /**
     * The formatting switches applied to this message.
     *
     * @return the flags, keyed by name
     */
    default @NonNull Map<MessageFlag, Boolean> flags() {
        return base().flags();
    }

    /**
     * The MiniMessage tags available while rendering this message.
     *
     * @return the resolver
     */
    default @NonNull TagResolver tagResolver() {
        return base().tagResolver();
    }

    /**
     * Who sent the message.
     *
     * @return the sender
     */
    default @NonNull FEntity sender() {
        return base().sender();
    }

    /**
     * Who this copy of the message is for.
     *
     * @return the receiver
     */
    default @NonNull FPlayer receiver() {
        return base().receiver();
    }

    /**
     * The id shared by every per-receiver copy of the same message.
     *
     * @return the message id
     */
    default @NonNull UUID uuid() {
        return base().uuid();
    }

    /**
     * The raw, unrendered text.
     *
     * @return the message text
     */
    default @NonNull String message() {
        return base().message();
    }

    /**
     * Returns a copy with a different base context.
     *
     * @param base the new base
     * @return the copy
     */
    default MessageContext withBase(@NonNull MessageContext base) {
        return base;
    }

    /**
     * Returns a copy with the flags replaced.
     *
     * @param flags the new flags
     * @return the copy
     */
    default MessageContext withFlags(@NonNull Map<MessageFlag, Boolean> flags) {
        return withBase(base().withFlags(flags));
    }

    /**
     * Returns a copy with the tag resolver replaced.
     *
     * @param tagResolver the new resolver
     * @return the copy
     */
    default MessageContext withTagResolver(@NonNull TagResolver tagResolver) {
        return withBase(base().withTagResolver(tagResolver));
    }

    /**
     * Returns a copy with a different sender.
     *
     * @param sender the new sender
     * @return the copy
     */
    default MessageContext withSender(@NonNull FEntity sender) {
        return withBase(base().withSender(sender));
    }

    /**
     * Returns a copy aimed at a different receiver.
     *
     * @param receiver the new receiver
     * @return the copy
     */
    default MessageContext withReceiver(@NonNull FPlayer receiver) {
        return withBase(base().withReceiver(receiver));
    }

    /**
     * Returns a copy with a different message id.
     *
     * @param uuid the new id
     * @return the copy
     */
    default MessageContext withUuid(@NonNull UUID uuid) {
        return withBase(base().withUuid(uuid));
    }

    /**
     * Returns a copy with different text.
     *
     * @param message the new text
     * @return the copy
     */
    default MessageContext withMessage(@NonNull String message) {
        return withBase(base().withMessage(message));
    }

    /**
     * Whether a flag is on, falling back to the flag's own default when it was never set.
     *
     * @param flag the flag to read
     * @return the flag state
     */
    default boolean isFlag(MessageFlag flag) {
        return flags().getOrDefault(flag, flag.getDefaultValue());
    }

    /**
     * Returns a copy with one flag set, keeping the rest.
     *
     * @param flag the flag to set
     * @param value the new state
     * @return the copy
     */
    default MessageContext addFlag(@NonNull MessageFlag flag, boolean value) {
        Map<MessageFlag, Boolean> newFlags = flags().isEmpty()
                ? new EnumMap<>(MessageFlag.class)
                : new EnumMap<>(flags());

        newFlags.put(flag, value);

        return withFlags(newFlags);
    }

    /**
     * Returns a copy with several flags set, keeping the rest.
     *
     * @param flags the flags to set
     * @param values the matching states
     * @return the copy, or this instance if either array is empty
     * @throws IllegalArgumentException if the two arrays differ in length
     */
    default MessageContext addFlags(MessageFlag @NonNull [] flags, boolean @NonNull [] values) {
        if (flags.length == 0 || values.length == 0) return this;

        if (flags.length != values.length) {
            throw new IllegalArgumentException("Flag and Value array lengths don't match: " + flags.length + " vs " + values.length);
        }

        Map<MessageFlag, Boolean> newFlags = flags().isEmpty()
                ? new EnumMap<>(MessageFlag.class)
                : new EnumMap<>(flags());

        for (int i = 0; i < flags.length; i++) {
            newFlags.put(flags[i], values[i]);
        }

        return withFlags(newFlags);
    }

    /**
     * Returns a copy with one more tag resolver layered on top of the current ones.
     *
     * @param tagResolver the resolver to add, may be null
     * @return the copy, or this instance if the resolver was null
     */
    default MessageContext addTagResolver(@Nullable TagResolver tagResolver) {
        if (tagResolver == null) return this;

        return withTagResolver(TagResolver.resolver(this.tagResolver(), tagResolver));
    }

    /**
     * Returns a copy with several tag resolvers layered on top of the current ones.
     *
     * @param resolvers the resolvers to add
     * @return the copy, or this instance if nothing was passed
     */
    default MessageContext addTagResolvers(@NonNull TagResolver... resolvers) {
        if (resolvers == null || resolvers.length == 0) return this;

        return withTagResolver(TagResolver.resolver(this.tagResolver(), TagResolver.resolver(resolvers)));
    }

    /**
     * Identifies a rendered message so an identical one can be served from cache instead of
     * being formatted again. Each wrapping context contributes its own data to the key.
     */
    interface CacheKey {

        /**
         * The key of the underlying context.
         *
         * @return the base key
         */
        CacheKey base();

    }

    /**
     * Builds the cache key for this context.
     *
     * @return the key
     */
    default CacheKey createCacheKey() {
        return base().createCacheKey();
    }

}