package net.flectone.pulse.model.event;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class Event {

    private boolean cancelled;

    protected Event() {
    }

    public enum Priority {
        LOWEST,
        LOW,
        NORMAL,
        HIGH,
        HIGHEST,
        MONITOR
    }
}
