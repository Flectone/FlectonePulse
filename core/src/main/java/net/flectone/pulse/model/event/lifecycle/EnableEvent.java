package net.flectone.pulse.model.event.lifecycle;

import lombok.With;
import net.flectone.pulse.FlectonePulse;
import net.flectone.pulse.model.event.Event;

@With
public record EnableEvent(
        boolean cancelled,
        FlectonePulse flectonePulse
) implements Event {

    public EnableEvent(FlectonePulse flectonePulse) {
        this(false, flectonePulse);
    }

}