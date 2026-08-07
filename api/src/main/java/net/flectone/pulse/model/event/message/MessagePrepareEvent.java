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

/**
 * Fired once the receivers are known but before anything is sent, so listeners can still
 * change the audience, the metadata or the wording.
 *
 * @param cancelled whether a listener vetoed the message
 * @param moduleName the module the message came from
 * @param eventMetadata how the message will be delivered
 * @param messageContext the message itself
 * @param integrationMessageFormat the format used when mirroring, or null if not mirrored
 * @param receivers the resolved audience
 * @author TheFaser
 */
@With
public record MessagePrepareEvent(
        boolean cancelled,
        @NonNull ModuleName moduleName,
        @NonNull EventMetadata eventMetadata,
        @NonNull MessageContext messageContext,
        @Nullable IntegrationMessageFormat integrationMessageFormat,
        @NonNull Set<FPlayer> receivers
) implements Event {


    /**
     * Creates an event that has not been cancelled and whose audience is not resolved yet.
     *
     * @param moduleName the module the message came from
     * @param eventMetadata how the message will be delivered
     * @param messageContext the message itself
     * @param integrationMessageFormat the mirroring format, or null
     */
    public MessagePrepareEvent(ModuleName moduleName, EventMetadata eventMetadata, MessageContext messageContext, IntegrationMessageFormat integrationMessageFormat) {
        this(false, moduleName, eventMetadata, messageContext, integrationMessageFormat, Set.of());
    }

    /**
     * Creates an event whose context and mirroring format are resolved from the metadata.
     *
     * @param moduleName the module the message came from
     * @param eventMetadata how the message will be delivered
     */
    public MessagePrepareEvent(ModuleName moduleName, EventMetadata eventMetadata) {
        this(false, moduleName, eventMetadata, eventMetadata.resolveMessageContext(FPlayer.UNKNOWN), eventMetadata.resolveIntegrationMessageFormat(), Set.of());
    }

}