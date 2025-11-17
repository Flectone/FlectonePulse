package net.flectone.pulse.model.event;

import lombok.Getter;
import net.flectone.pulse.exception.ReloadException;

@Getter
public class ReloadEvent extends Event {

    private final ReloadException reloadException;

    public ReloadEvent(ReloadException reloadException) {
        this.reloadException = reloadException;
    }

    public boolean isSuccessful() {
        return reloadException == null;
    }

}
