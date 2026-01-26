package net.flectone.pulse.model.event;

import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Destination;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.model.util.Sound;
import net.flectone.pulse.util.ProxyDataConsumer;
import net.flectone.pulse.util.SafeDataOutputStream;
import net.flectone.pulse.util.constant.MessageFlag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.incendo.cloud.type.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public interface EventMetadata<L extends LocalizationSetting> {

    EventMetadata<L> base();

    default @NonNull UUID uuid() {
        return base().uuid();
    }

    default @NonNull FEntity sender() {
        return base().sender();
    }

    default @Nullable FPlayer filterPlayer() {
        return base().filterPlayer();
    }

    default @NonNull Predicate<FPlayer> filter() {
        return base().filter();
    }

    default @NonNull Map<MessageFlag, Boolean> flags() {
        return base().flags();
    }

    default @NonNull BiFunction<FPlayer, L, String> format() {
        return base().format();
    }

    default @NonNull Destination destination() {
        return base().destination();
    }

    default @NonNull Range range() {
        return base().range();
    }

    default @Nullable Pair<Sound, PermissionSetting> sound() {
        return base().sound();
    }

    default @Nullable String message() {
        return base().message();
    }

    default @Nullable Function<FPlayer, TagResolver[]> tagResolvers() {
        return base().tagResolvers();
    }

    default @Nullable ProxyDataConsumer<SafeDataOutputStream> proxy() {
        return base().proxy();
    }

    default @Nullable UnaryOperator<String> integration() {
        return base().integration();
    }

    default @Nullable TagResolver[] resolveTags(FPlayer player) {
        return base().resolveTags(player);
    }

    default @NonNull String resolveFormat(FPlayer player, L localization) {
        return base().resolveFormat(player, localization);
    }

    static <L extends LocalizationSetting> Builder<L> builder() {
        return new Builder<>();
    }

    final class Builder<L extends LocalizationSetting> {

        private final Map<MessageFlag, Boolean> flags = new EnumMap<>(MessageFlag.class);

        private UUID uuid = UUID.randomUUID();
        private FEntity sender;
        private FPlayer filterPlayer;
        private Predicate<FPlayer> filter = p -> true;
        private BiFunction<FPlayer, L, String> format;
        private Destination destination = Destination.EMPTY_CHAT;
        private Range range;
        private Pair<Sound, PermissionSetting> sound;
        private String message;
        private Function<FPlayer, TagResolver[]> tagResolvers;
        private ProxyDataConsumer<SafeDataOutputStream> proxy;
        private UnaryOperator<String> integration;

        private Builder() {
        }

        public Builder<L> uuid(UUID uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder<L> sender(FEntity sender) {
            this.sender = sender;
            return filterPlayer(sender);
        }

        public Builder<L> filterPlayer(FEntity entity) {
            this.range = Range.get(Range.Type.PLAYER);
            this.filterPlayer = (entity instanceof FPlayer fp) ? fp : FPlayer.UNKNOWN;
            return this;
        }

        public Builder<L> filterPlayer(FPlayer player) {
            this.filterPlayer = player;
            return this;
        }

        public Builder<L> filterPlayer(FPlayer player, boolean senderColorOut) {
            return filterPlayer(player).flag(MessageFlag.SENDER_COLOR_OUT, senderColorOut);
        }

        public Builder<L> filter(Predicate<FPlayer> filter) {
            this.filter = filter;
            return this;
        }

        public Builder<L> flag(MessageFlag messageFlag, boolean value) {
            flags.put(messageFlag, value);
            return this;
        }

        public Builder<L> format(String staticFormat) {
            this.format = (p, loc) -> staticFormat;
            return this;
        }

        public Builder<L> format(Function<L, String> formatFn) {
            this.format = (p, loc) -> formatFn.apply(loc);
            return this;
        }

        public Builder<L> format(BiFunction<FPlayer, L, String> formatFn) {
            this.format = formatFn;
            return this;
        }

        public Builder<L> destination(Destination destination) {
            this.destination = destination;
            return this;
        }

        public Builder<L> range(Range range) {
            this.range = range;
            return this;
        }

        public Builder<L> sound(Pair<Sound, PermissionSetting> sound) {
            this.sound = sound;
            return this;
        }

        public Builder<L> message(String message) {
            this.message = message;
            return this;
        }

        public Builder<L> tagResolvers(Function<FPlayer, TagResolver[]> tagResolvers) {
            this.tagResolvers = tagResolvers;
            return this;
        }

        public Builder<L> proxy(ProxyDataConsumer<SafeDataOutputStream> proxy) {
            this.proxy = proxy;
            return this;
        }

        public Builder<L> proxy() {
            this.proxy = out -> {};
            return this;
        }

        public Builder<L> integration(UnaryOperator<String> integration) {
            this.integration = integration;
            return this;
        }

        public Builder<L> integration() {
            this.integration = s -> s;
            return this;
        }

        public EventMetadata<L> build() {
            Objects.requireNonNull(sender);
            Objects.requireNonNull(format);
            Objects.requireNonNull(range);

            return new BaseEventMetadata<>(
                    uuid,
                    sender,
                    filterPlayer,
                    filter,
                    Collections.unmodifiableMap(flags),
                    format,
                    destination,
                    range,
                    sound,
                    message,
                    tagResolvers,
                    proxy,
                    integration
            );
        }
    }
}

