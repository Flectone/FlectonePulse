package net.flectone.pulse.platform.registry;

import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import org.jspecify.annotations.NonNull;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * Owns both the plugin's own event listeners and the ones registered with the platform.
 * @author TheFaser
 */
public interface ListenerRegistry extends Registry {

    /**
     * Every registered plugin listener, grouped by event and priority.
     *
     * @return the listeners
     */
    @NonNull Map<Class<? extends Event>, EnumMap<Event.Priority, List<UnaryOperator<Event>>>> getPulseListeners();

    /**
     * The listeners for one event, grouped by priority.
     *
     * @param event the event type
     * @return the listeners, empty if none are registered
     */
    @NonNull Map<Event.Priority, List<UnaryOperator<Event>>> getPulseListeners(Class<? extends Event> event);

    /**
     * Registers a listener that survives reloads, for listeners owned by other plugins.
     *
     * @param pulseListener the listener
     */
    void registerPermanent(PulseListener pulseListener);

    /**
     * Registers a listener by type, creating it through dependency injection.
     *
     * @param clazzListener the listener type
     */
    void register(Class<?> clazzListener);

    /**
     * Registers a listener by type at a given priority.
     *
     * @param clazzListener the listener type
     * @param eventPriority when it runs relative to the others
     */
    void register(Class<?> clazzListener, Event.Priority eventPriority);

    /**
     * Registers an already built listener.
     *
     * @param pulseListener the listener
     */
    void register(PulseListener pulseListener);

    /**
     * Registers a single handler for one event.
     *
     * @param eventClass the event type
     * @param priority when it runs relative to the others
     * @param handler handles the event and returns the possibly modified one
     */
    void register(Class<? extends Event> eventClass, Event.Priority priority, UnaryOperator<Event> handler);

    /**
     * Removes every listener, including the permanent ones.
     */
    void unregisterAll();

    /**
     * Registers the listeners the plugin always needs.
     */
    void registerDefaultListeners();

}