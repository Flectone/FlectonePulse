package net.flectone.pulse.model.event.lifecycle;

import lombok.Getter;
import net.flectone.pulse.FlectonePulse;
import net.flectone.pulse.exception.ReloadException;
import net.flectone.pulse.model.event.Event;

@Getter
public class ReloadEvent extends Event {

    private final FlectonePulse flectonePulse;
    private final ReloadException reloadException;

    public ReloadEvent(FlectonePulse flectonePulse, ReloadException reloadException) {
        this.flectonePulse = flectonePulse;
        this.reloadException = reloadException;
    }

    public boolean isSuccessful() {
        return reloadException == null;
    }

}
