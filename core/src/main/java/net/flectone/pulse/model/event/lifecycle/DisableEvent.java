package net.flectone.pulse.model.event.lifecycle;

import lombok.With;
import net.flectone.pulse.FlectonePulse;
import net.flectone.pulse.model.event.Event;

@With
public record DisableEvent(
        boolean cancelled,
        FlectonePulse flectonePulse
) implements Event {

    public DisableEvent(FlectonePulse flectonePulse) {
        this(false, flectonePulse);
    }

}