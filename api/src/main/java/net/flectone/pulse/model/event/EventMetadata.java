package net.flectone.pulse.model.event;

import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.util.Destination;
import net.flectone.pulse.model.util.Pair;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.model.util.Sound;
import net.flectone.pulse.util.ProxyDataConsumer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.DataOutputStream;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Everything the dispatcher needs to deliver a message besides its text, namely who receives it,
 * where it is shown, how far it travels and which sound plays.
 * @author TheFaser
 */
public interface EventMetadata {

    /**
     * Starts building event metadata.
     *
     * @return a new builder
     */
    static Builder builder() {
        return new Builder();
    }

    /**
     * The underlying metadata this instance delegates to.
     *
     * @return the base metadata
     */
    EventMetadata base();

    /**
     * Decides which players receive the message.
     *
     * @return the receiver filter
     */
    default @NonNull Predicate<FPlayer> filter() {
        return base().filter();
    }

    /**
     * Where on the client the message is drawn.
     *
     * @return the destination
     */
    default @NonNull Destination destination() {
        return base().destination();
    }

    /**
     * How far the message travels from its sender.
     *
     * @return the range
     */
    default @NonNull Range range() {
        return base().range();
    }

    /**
     * The sound played with the message, paired with the permission that allows hearing it.
     *
     * @return the sound and its permission, or null if no sound is played
     */
    default @Nullable Pair<Sound, PermissionSetting> sound() {
        return base().sound();
    }

    /**
     * Writes the extra payload sent to other servers when the range reaches the proxy.
     *
     * @return the payload writer, or null if the message never leaves this server
     */
    default @Nullable ProxyDataConsumer<DataOutputStream> proxy() {
        return base().proxy();
    }

    /**
     * Supplies the formatting used when the message is mirrored to Discord, Telegram or Twitch.
     *
     * @return the integration format supplier, or null if the message is not mirrored
     */
    default @Nullable Supplier<IntegrationMessageFormat> integration() {
        return base().integration();
    }

    /**
     * Builds the message context for one receiver, so each player can get their own wording.
     *
     * @param fPlayer the receiving player
     * @return the context for that player
     */
    default @NonNull MessageContext resolveMessageContext(FPlayer fPlayer) {
        return base().resolveMessageContext(fPlayer);
    }

    /**
     * Resolves the integration format, if one was supplied.
     *
     * @return the format, or null if the message is not mirrored
     */
    default @Nullable IntegrationMessageFormat resolveIntegrationMessageFormat() {
        Supplier<IntegrationMessageFormat> integrationMessageFormatSupplier = base().integration();
        if (integrationMessageFormatSupplier == null) return null;

        return integrationMessageFormatSupplier.get();
    }

    /**
     * Collects the metadata pieces. Receiver filters stack, so each call narrows the audience further.
     */
    final class Builder {

        private Predicate<FPlayer> filter = _ -> true;
        private Destination destination = Destination.EMPTY_CHAT;
        private Range range = Range.get(Range.Type.PLAYER);
        private Pair<Sound, PermissionSetting> sound;
        private Function<FPlayer, MessageContext> messageContext;
        private ProxyDataConsumer<DataOutputStream> proxy;
        private Supplier<IntegrationMessageFormat> integration;

        private Builder() {
        }

        /**
         * Sets how the message context is built for each receiver. Required.
         *
         * @param messageContext builds the context for a given receiver
         * @return this builder
         */
        public Builder messageContext(Function<FPlayer, MessageContext> messageContext) {
            this.messageContext = messageContext;
            return this;
        }

        /**
         * Narrows delivery to a single player.
         *
         * @param fReceiver the only receiver
         * @return this builder
         */
        public Builder filter(FPlayer fReceiver) {
            return filter(fReceiver::equals);
        }

        /**
         * Narrows delivery to the given players.
         *
         * @param fReceivers the receivers
         * @return this builder
         */
        public Builder filter(Collection<FPlayer> fReceivers) {
            return filter(fReceivers::contains);
        }

        /**
         * Narrows delivery further. The new condition is combined with the existing one.
         *
         * @param filter the extra condition a receiver must satisfy
         * @return this builder
         */
        public Builder filter(Predicate<FPlayer> filter) {
            this.filter = this.filter.and(filter);
            return this;
        }

        /**
         * Sets where the message is drawn. Defaults to chat.
         *
         * @param destination the destination
         * @return this builder
         */
        public Builder destination(Destination destination) {
            this.destination = destination;
            return this;
        }

        /**
         * Sets the range to one of the named scopes.
         *
         * @param type the scope
         * @return this builder
         */
        public Builder range(Range.@NonNull Type type) {
            this.range = Range.get(type);
            return this;
        }

        /**
         * Sets the range. Defaults to the sender only.
         *
         * @param range the range
         * @return this builder
         */
        public Builder range(@NonNull Range range) {
            this.range = range;
            return this;
        }

        /**
         * Sets the sound played with the message and the permission needed to hear it.
         *
         * @param sound the sound and its permission
         * @return this builder
         */
        public Builder sound(Pair<Sound, PermissionSetting> sound) {
            this.sound = sound;
            return this;
        }

        /**
         * Sends the message across the proxy, writing the given extra payload.
         *
         * @param proxy writes the extra payload
         * @return this builder
         */
        public Builder proxy(ProxyDataConsumer<DataOutputStream> proxy) {
            this.proxy = proxy;
            return this;
        }

        /**
         * Sends the message across the proxy with no extra payload.
         *
         * @return this builder
         */
        public Builder proxy() {
            this.proxy = _ -> {};
            return this;
        }

        /**
         * Mirrors the message to the chat integrations using the supplied format.
         *
         * @param integration supplies the format
         * @return this builder
         */
        public Builder integration(Supplier<IntegrationMessageFormat> integration) {
            this.integration = integration;
            return this;
        }

        /**
         * Mirrors the message to the chat integrations using the default format.
         *
         * @return this builder
         */
        public Builder integration() {
            this.integration = () -> IntegrationMessageFormat.builder().build();
            return this;
        }

        /**
         * Builds the metadata.
         *
         * @return the built metadata
         * @throws NullPointerException if no message context was set
         */
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

