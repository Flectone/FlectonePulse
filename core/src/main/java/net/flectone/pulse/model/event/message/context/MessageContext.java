package net.flectone.pulse.model.event.message.context;

import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.util.constant.MessageFlag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.ArrayUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public interface MessageContext {

    static MessageContextImpl.MessageContextImplBuilder builder() {
        return MessageContextImpl.builder();
    }

    MessageContext base();

    default MessageContextImpl.MessageContextImplBuilder toBuilder() {
        return base().toBuilder();
    }

    default @NonNull Map<MessageFlag, Boolean> flags() {
        return base().flags();
    }

    default @NonNull TagResolver tagResolver() {
        return base().tagResolver();
    }

    default @NonNull FEntity sender() {
        return base().sender();
    }

    default @NonNull FPlayer receiver() {
        return base().receiver();
    }

    default @NonNull UUID uuid() {
        return base().uuid();
    }

    default @NonNull String message() {
        return base().message();
    }

    default MessageContext withBase(@NonNull MessageContext base) {
        return base;
    }

    default MessageContext withFlags(@NonNull Map<MessageFlag, Boolean> flags) {
        return withBase(base().withFlags(flags));
    }

    default MessageContext withTagResolver(@NonNull TagResolver tagResolver) {
        return withBase(base().withTagResolver(tagResolver));
    }

    default MessageContext withSender(@NonNull FEntity sender) {
        return withBase(base().withSender(sender));
    }

    default MessageContext withReceiver(@NonNull FPlayer receiver) {
        return withBase(base().withReceiver(receiver));
    }

    default MessageContext withUuid(@NonNull UUID uuid) {
        return withBase(base().withUuid(uuid));
    }

    default MessageContext withMessage(@NonNull String message) {
        return withBase(base().withMessage(message));
    }

    default boolean isFlag(MessageFlag flag) {
        return flags().getOrDefault(flag, flag.getDefaultValue());
    }

    default MessageContext addFlag(@NonNull MessageFlag flag, boolean value) {
        Map<MessageFlag, Boolean> newFlags = flags().isEmpty()
                ? new EnumMap<>(MessageFlag.class)
                : new EnumMap<>(flags());

        newFlags.put(flag, value);

        return withFlags(newFlags);
    }

    default MessageContext addFlags(MessageFlag @NonNull [] flags, boolean @NonNull [] values) {
        if (ArrayUtils.isEmpty(flags) || ArrayUtils.isEmpty(values)) return this;

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

    default MessageContext addTagResolver(@Nullable TagResolver tagResolver) {
        if (tagResolver == null) return this;

        return withTagResolver(TagResolver.resolver(this.tagResolver(), tagResolver));
    }

    default MessageContext addTagResolvers(@NonNull TagResolver... resolvers) {
        if (resolvers == null || resolvers.length == 0) return this;

        return withTagResolver(TagResolver.resolver(this.tagResolver(), TagResolver.resolver(resolvers)));
    }

    interface CacheKey {

        CacheKey base();

    }

    default CacheKey createCacheKey() {
        return base().createCacheKey();
    }

}