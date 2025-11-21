package net.flectone.pulse.model.event.lifecycle;

import lombok.Getter;
import net.flectone.pulse.FlectonePulse;
import net.flectone.pulse.model.event.Event;

@Getter
public class EnableEvent extends Event {

    private final FlectonePulse flectonePulse;

    public EnableEvent(FlectonePulse flectonePulse) {
        this.flectonePulse = flectonePulse;
    }

}
