package net.flectone.pulse.model.event;

import lombok.Builder;
import lombok.With;
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

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@With
@Builder
record EventMetadataImpl(
        @NonNull Predicate<FPlayer> filter,
        @NonNull Destination destination,
        @NonNull Range range,
        @Nullable Pair<Sound, PermissionSetting> sound,
        @NonNull Function<FPlayer, MessageContext> messageContext,
        @Nullable ProxyDataConsumer<SafeDataOutputStream> proxy,
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
