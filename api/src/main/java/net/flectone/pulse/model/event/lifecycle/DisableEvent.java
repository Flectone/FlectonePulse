package net.flectone.pulse.model.event.lifecycle;

import lombok.With;
import net.flectone.pulse.FlectonePulse;
import net.flectone.pulse.model.event.Event;

/**
 * Fired while the plugin is shutting down, before anything is torn down.
 *
 * @param cancelled whether a listener vetoed the shutdown work
 * @param flectonePulse the plugin instance
 * @author TheFaser
 */
@With
public record DisableEvent(
        boolean cancelled,
        FlectonePulse flectonePulse
) implements Event {

    /**
     * Creates an event that has not been cancelled.
     *
     * @param flectonePulse the plugin instance
     */
    public DisableEvent(FlectonePulse flectonePulse) {
        this(false, flectonePulse);
    }

}