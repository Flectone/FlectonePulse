package net.flectone.pulse.model.event;

/**
 * Something that happened inside FlectonePulse and that a listener may inspect or veto.
 * Events are immutable records; a listener that wants to change one returns a modified copy.
 * @author TheFaser
 */
public interface Event {

    /**
     * Whether a listener has vetoed this event. A cancelled event stops before its side effect runs.
     *
     * @return true if the event was cancelled
     */
    boolean cancelled();

    /**
     * Returns a copy of this event with the cancelled flag set.
     *
     * @param cancelled the new cancelled state
     * @param <T> the concrete event type
     * @return a copy carrying the new state
     */
    <T extends Event> T withCancelled(boolean cancelled);

    /**
     * The order listeners run in. Lower priorities run first, and {@code MONITOR} runs last and is
     * meant for observing the final outcome rather than changing it.
     */
    enum Priority {

        LOWEST,
        LOW,
        NORMAL,
        HIGH,
        HIGHEST,
        MONITOR

    }

}
