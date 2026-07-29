package net.flectone.pulse.model.event.message;

import lombok.With;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.IntegrationMessageFormat;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.util.constant.ModuleName;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Set;

@With
public record MessagePrepareEvent(
        boolean cancelled,
        @NonNull ModuleName moduleName,
        @NonNull EventMetadata eventMetadata,
        @NonNull MessageContext messageContext,
        @Nullable IntegrationMessageFormat integrationMessageFormat,
        @NonNull Set<FPlayer> receivers
) implements Event {


    public MessagePrepareEvent(ModuleName moduleName, EventMetadata eventMetadata, MessageContext messageContext, IntegrationMessageFormat integrationMessageFormat) {
        this(false, moduleName, eventMetadata, messageContext, integrationMessageFormat, Set.of());
    }

    public MessagePrepareEvent(ModuleName moduleName, EventMetadata eventMetadata) {
        this(false, moduleName, eventMetadata, eventMetadata.resolveMessageContext(FPlayer.UNKNOWN), eventMetadata.resolveIntegrationMessageFormat(), Set.of());
    }

}