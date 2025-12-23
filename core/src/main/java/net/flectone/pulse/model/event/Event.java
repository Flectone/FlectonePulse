package net.flectone.pulse.model.event;

public interface Event {

    boolean cancelled();

    <T extends Event> T withCancelled(boolean cancelled);

    enum Priority {

        LOWEST,
        LOW,
        NORMAL,
        HIGH,
        HIGHEST,
        MONITOR

    }

}
