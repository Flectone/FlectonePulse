package net.flectone.pulse.dispatcher;

import net.flectone.pulse.model.event.Event;

import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * Hands an event to the registered listeners, in priority order, and returns whatever they made of it.
 * @author TheFaser
 */
public interface EventDispatcher {

    /**
     * Runs an event through an explicit set of listeners, used to replay listeners captured before a reload.
     *
     * @param listeners the listeners to run, grouped by priority
     * @param event the event
     * @param <T> the event type
     * @return the event as the listeners left it
     */
    <T extends Event> T dispatch(Map<Event.Priority, List<UnaryOperator<Event>>> listeners, T event);

    /**
     * Runs an event through every registered listener.
     *
     * @param event the event
     * @param <T> the event type
     * @return the event as the listeners left it
     */
    <T extends Event> T dispatch(T event);

}