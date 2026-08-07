package net.flectone.pulse.model.event;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.value.Destination;
import net.flectone.pulse.model.value.Pair;
import net.flectone.pulse.model.value.Range;
import net.flectone.pulse.model.value.Sound;
import net.flectone.pulse.util.ProxyDataConsumer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.DataOutputStream;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * The plain event metadata every builder produces.
 *
 * @param filter decides which players receive the message
 * @param destination where the message is drawn
 * @param range how far it travels
 * @param sound the sound and the permission to hear it, or null
 * @param messageContext builds the context for a given receiver
 * @param proxy writes the extra proxy payload, or null
 * @param integration supplies the mirroring format, or null
 */
@With
@Builder
public record EventMetadataImpl(
        @NonNull Predicate<FPlayer> filter,
        @NonNull Destination destination,
        @NonNull Range range,
        @Nullable Pair<Sound, PermissionSetting> sound,
        @NonNull Function<FPlayer, MessageContext> messageContext,
        @Nullable ProxyDataConsumer<DataOutputStream> proxy,
        @Nullable Supplier<IntegrationMessageFormat> integration
) implements EventMetadata {

    @Override
    public EventMetadataImpl base() {
        return this;
    }

    @Override
    public @NonNull MessageContext resolveMessageContext(FPlayer fPlayer) {
        return messageContext.apply(fPlayer);
    }

}
