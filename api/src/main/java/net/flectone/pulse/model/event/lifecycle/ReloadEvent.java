package net.flectone.pulse.model.event.lifecycle;

import lombok.With;
import net.flectone.pulse.FlectonePulse;
import net.flectone.pulse.exception.ReloadException;
import net.flectone.pulse.model.event.Event;
import org.jspecify.annotations.Nullable;

/**
 * Fired after a reload finishes, whether or not it succeeded.
 *
 * @param cancelled whether a listener vetoed the remaining reload work
 * @param type the phase of the reload cycle that this event represents
 * @param flectonePulse the plugin instance
 * @param reloadException the failure that occurred, or null if the reload succeeded
 * @author TheFaser
 */
@With
public record ReloadEvent(
        boolean cancelled,
        Type type,
        FlectonePulse flectonePulse,
        @Nullable ReloadException reloadException
) implements Event {

    /**
     * Creates an event that has not been cancelled.
     *
     * @param flectonePulse the plugin instance
     * @param type the phase of the reload cycle that this event represents
     * @param reloadException the failure that occurred, or null on success
     */
    public ReloadEvent(FlectonePulse flectonePulse, Type type, ReloadException reloadException) {
        this(false, type, flectonePulse, reloadException);
    }

    /**
     * Creates an event that has not been cancelled and represents a successful reload.
     *
     * @param flectonePulse the plugin instance
     * @param type the phase of the reload cycle that this event represents
     */
    public ReloadEvent(FlectonePulse flectonePulse, Type type) {
        this(false, type, flectonePulse, null);
    }

    /**
     * Whether the reload completed without a failure.
     *
     * @return true if no exception was recorded
     */
    public boolean isSuccessful() {
        return reloadException == null;
    }

    /**
     * Represents the phase of a reload cycle.
     */
    public enum Type {
        /** Fired when the reload is about to begin. */
        START,

        /** Fired after the reload has completed. */
        END
    }

}