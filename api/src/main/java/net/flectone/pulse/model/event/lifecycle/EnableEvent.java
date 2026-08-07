package net.flectone.pulse.model.event.lifecycle;

import lombok.With;
import net.flectone.pulse.FlectonePulse;
import net.flectone.pulse.model.event.Event;

/**
 * Fired while the plugin starts up, once per {@link Type} stage.
 *
 * @param cancelled whether a listener vetoed the remaining startup work
 * @param type the startup stage
 * @param flectonePulse the plugin instance
 * @author TheFaser
 */
@With
public record EnableEvent(
        boolean cancelled,
        Type type,
        FlectonePulse flectonePulse
) implements Event {

    /**
     * Creates an event that has not been cancelled.
     *
     * @param type the startup stage
     * @param flectonePulse the plugin instance
     */
    public EnableEvent(Type type, FlectonePulse flectonePulse) {
        this(false, type, flectonePulse);
    }

    /**
     * The startup stage. {@code INIT} runs before services come up, {@code READY} once everything is running.
     */
    public enum Type {
        INIT,
        READY
    }

}