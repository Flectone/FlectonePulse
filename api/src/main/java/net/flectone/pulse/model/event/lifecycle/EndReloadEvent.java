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
 * @param flectonePulse the plugin instance
 * @param reloadException the failure that occurred, or null if the reload succeeded
 * @author TheFaser
 */
@With
public record EndReloadEvent(
        boolean cancelled,
        FlectonePulse flectonePulse,
        @Nullable ReloadException reloadException
) implements Event {

    /**
     * Creates an event that has not been cancelled.
     *
     * @param flectonePulse the plugin instance
     * @param reloadException the failure that occurred, or null on success
     */
    public EndReloadEvent(FlectonePulse flectonePulse, ReloadException reloadException) {
        this(false, flectonePulse, reloadException);
    }

    /**
     * Whether the reload completed without a failure.
     *
     * @return true if no exception was recorded
     */
    public boolean isSuccessful() {
        return reloadException == null;
    }

}