package net.flectone.pulse.model.event.message;

import lombok.With;
import net.flectone.pulse.model.event.Event;

/**
 * Fired when the server answers a status ping, so the MOTD and player counts can be rewritten.
 *
 * @param cancelled whether a listener vetoed the change
 * @param response the platform-specific response object
 * @author TheFaser
 */
@With
public record StatusResponseEvent(
        boolean cancelled,
        Object response
) implements Event {

    /**
     * Creates an event that has not been cancelled.
     *
     * @param response the platform-specific response object
     */
    public StatusResponseEvent(Object response) {
        this(false, response);
    }

}