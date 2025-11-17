package net.flectone.pulse.execution.dispatcher;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.platform.registry.ListenerRegistry;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class EventDispatcher {

    private final ListenerRegistry listenerRegistry;

    public void dispatch(Map<Event.Priority, List<Consumer<Event>>> listeners, Event event) {
        if (listeners == null) return;

        // LOWEST -> LOW -> NORMAL -> HIGH -> HIGHEST -> MONITOR
        for (Event.Priority priority : Event.Priority.values()) {
            List<Consumer<Event>> handlersList = listeners.get(priority);
            if (handlersList != null) {
                handlersList.forEach(handler -> handler.accept(event));
            }
        }
    }

    public void dispatch(Event event) {
        dispatch(listenerRegistry.getPulseListeners().get(event.getClass()), event);
    }

}
