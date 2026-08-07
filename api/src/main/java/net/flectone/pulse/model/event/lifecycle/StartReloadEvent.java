package net.flectone.pulse.model.event.lifecycle;

import lombok.With;
import net.flectone.pulse.FlectonePulse;
import net.flectone.pulse.model.event.Event;

/**
 * Fired before a reload begins. Cancelling it aborts the reload.
 *
 * @param cancelled whether a listener vetoed the reload
 * @param flectonePulse the plugin instance
 * @author TheFaser
 */
@With
public record StartReloadEvent(
        boolean cancelled,
        FlectonePulse flectonePulse
) implements Event {

    /**
     * Creates an event that has not been cancelled.
     *
     * @param flectonePulse the plugin instance
     */
    public StartReloadEvent(FlectonePulse flectonePulse) {
        this(false, flectonePulse);
    }

}