package net.flectone.pulse.execution.dispatcher;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import org.jetbrains.annotations.CheckReturnValue;

import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class EventDispatcher {

    private final ListenerRegistry listenerRegistry;

    @CheckReturnValue
    @SuppressWarnings("unchecked")
    public <T extends Event> T dispatch(Map<Event.Priority, List<UnaryOperator<Event>>> listeners, T event) {
        if (listeners == null) return event;

        T currentEvent = event;

        for (Event.Priority priority : Event.Priority.values()) {
            List<UnaryOperator<Event>> handlersList = listeners.get(priority);
            if (handlersList != null) {
                for (UnaryOperator<Event> handler : handlersList) {
                    currentEvent = (T) handler.apply(currentEvent);
                }
            }
        }

        return currentEvent;
    }

    @CheckReturnValue
    public <T extends Event> T dispatch(T event) {
        return dispatch(listenerRegistry.getPulseListeners().get(event.getClass()), event);
    }

}