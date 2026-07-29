package net.flectone.pulse.model.event;

import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.util.Destination;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.model.util.Sound;
import net.flectone.pulse.util.ProxyDataConsumer;
import net.flectone.pulse.util.SafeDataOutputStream;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface EventMetadata {

    static Builder builder() {
        return new Builder();
    }

    EventMetadata base();

    default @NonNull Predicate<FPlayer> filter() {
        return base().filter();
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

    default @Nullable ProxyDataConsumer<SafeDataOutputStream> proxy() {
        return base().proxy();
    }

    default @Nullable Supplier<IntegrationMessageFormat> integration() {
        return base().integration();
    }

    default @NonNull MessageContext resolveMessageContext(FPlayer fPlayer) {
        return base().resolveMessageContext(fPlayer);
    }

    default @Nullable IntegrationMessageFormat resolveIntegrationMessageFormat() {
        Supplier<IntegrationMessageFormat> integrationMessageFormatSupplier = base().integration();
        if (integrationMessageFormatSupplier == null) return null;

        return integrationMessageFormatSupplier.get();
    }

    final class Builder {

        private Predicate<FPlayer> filter = _ -> true;
        private Destination destination = Destination.EMPTY_CHAT;
        private Range range = Range.get(Range.Type.PLAYER);
        private Pair<Sound, PermissionSetting> sound;
        private Function<FPlayer, MessageContext> messageContext;
        private ProxyDataConsumer<SafeDataOutputStream> proxy;
        private Supplier<IntegrationMessageFormat> integration;

        private Builder() {
        }

        public Builder messageContext(Function<FPlayer, MessageContext> messageContext) {
            this.messageContext = messageContext;
            return this;
        }

        public Builder filter(FPlayer fReceiver) {
            return filter(fReceiver::equals);
        }

        public Builder filter(Collection<FPlayer> fReceivers) {
            return filter(fReceivers::contains);
        }

        public Builder filter(Predicate<FPlayer> filter) {
            this.filter = this.filter.and(filter);
            return this;
        }

        public Builder destination(Destination destination) {
            this.destination = destination;
            return this;
        }

        public Builder range(Range.@NonNull Type type) {
            this.range = Range.get(type);
            return this;
        }

        public Builder range(@NonNull Range range) {
            this.range = range;
            return this;
        }

        public Builder sound(Pair<Sound, PermissionSetting> sound) {
            this.sound = sound;
            return this;
        }

        public Builder proxy(ProxyDataConsumer<SafeDataOutputStream> proxy) {
            this.proxy = proxy;
            return this;
        }

        public Builder proxy() {
            this.proxy = _ -> {};
            return this;
        }

        public Builder integration(Supplier<IntegrationMessageFormat> integration) {
            this.integration = integration;
            return this;
        }

        public Builder integration() {
            this.integration = () -> IntegrationMessageFormat.builder().build();
            return this;
        }

        public EventMetadataImpl build() {
            Objects.requireNonNull(messageContext);

            return new EventMetadataImpl(
                    filter,
                    destination,
                    range,
                    sound,
                    messageContext,
                    proxy,
                    integration
            );
        }
    }
}

