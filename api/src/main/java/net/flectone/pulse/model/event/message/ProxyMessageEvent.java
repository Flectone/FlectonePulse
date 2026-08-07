package net.flectone.pulse.model.event.message;

import lombok.With;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.util.payload.ProxyPayload;

import java.util.UUID;

/**
 * Fired when a message arrives from another server over the proxy channel.
 *
 * @param cancelled whether a listener vetoed handling
 * @param processed whether a listener already handled it, so the default handling is skipped
 * @param sentByThisServer whether this server is the origin, echoed back by the proxy
 * @param server the originating server name
 * @param name the module the message came from
 * @param sender the entity that sent it
 * @param uuid the id shared by every copy of this message
 * @param payload the raw extra data written by the sender
 * @author TheFaser
 */
@With
public record ProxyMessageEvent(
        boolean cancelled,
        boolean processed,
        boolean sentByThisServer,
        String server,
        ModuleName name,
        FEntity sender,
        UUID uuid,
        byte[] payload
) implements Event {

    /**
     * Creates an event that has been neither cancelled nor processed.
     *
     * @param sentByThisServer whether this server is the origin
     * @param server the originating server name
     * @param name the module the message came from
     * @param sender the entity that sent it
     * @param uuid the shared message id
     * @param payload the raw extra data
     */
    public ProxyMessageEvent(boolean sentByThisServer, String server, ModuleName name, FEntity sender, UUID uuid, byte[] payload) {
        this(false, false, sentByThisServer, server, name, sender, uuid, payload);
    }

    /**
     * Opens the extra data for reading, in the same order the sender wrote it.
     *
     * @return a reader over the payload
     */
    public ProxyPayload openPayload() {
        return new ProxyPayload(payload);
    }

}
