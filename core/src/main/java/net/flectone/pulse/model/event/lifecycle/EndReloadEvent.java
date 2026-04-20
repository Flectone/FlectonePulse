package net.flectone.pulse.model.event.lifecycle;

import lombok.With;
import net.flectone.pulse.FlectonePulse;
import net.flectone.pulse.exception.ReloadException;
import net.flectone.pulse.model.event.Event;
import org.jspecify.annotations.Nullable;

@With
public record EndReloadEvent(
        boolean cancelled,
        FlectonePulse flectonePulse,
        @Nullable ReloadException reloadException
) implements Event {

    public EndReloadEvent(FlectonePulse flectonePulse, ReloadException reloadException) {
        this(false, flectonePulse, reloadException);
    }

    public boolean isSuccessful() {
        return reloadException == null;
    }

}