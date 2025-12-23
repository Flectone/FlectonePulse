package net.flectone.pulse.model.event.lifecycle;

import lombok.With;
import net.flectone.pulse.FlectonePulse;
import net.flectone.pulse.exception.ReloadException;
import net.flectone.pulse.model.event.Event;

@With
public record ReloadEvent(
        boolean cancelled,
        FlectonePulse flectonePulse,
        ReloadException reloadException
) implements Event {

    public ReloadEvent(FlectonePulse flectonePulse, ReloadException reloadException) {
        this(false, flectonePulse, reloadException);
    }

    public boolean isSuccessful() {
        return reloadException == null;
    }

}