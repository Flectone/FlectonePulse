package net.flectone.pulse.model.event.lifecycle;

import lombok.With;
import net.flectone.pulse.FlectonePulse;
import net.flectone.pulse.model.event.Event;

@With
public record EnableEvent(
        boolean cancelled,
        Type type,
        FlectonePulse flectonePulse
) implements Event {

    public EnableEvent(Type type, FlectonePulse flectonePulse) {
        this(false, type, flectonePulse);
    }

    public enum Type {
        INIT,
        READY
    }

}