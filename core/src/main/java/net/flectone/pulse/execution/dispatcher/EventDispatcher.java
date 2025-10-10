package net.flectone.pulse.execution.dispatcher;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.platform.registry.ListenerRegistry;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Consumer;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class EventDispatcher {

    private final ListenerRegistry listenerRegistry;

    public void dispatch(Event event) {
        EnumMap<Event.Priority, List<Consumer<Event>>> priorityMap = listenerRegistry.getPulseListeners().get(event.getClass());
        if (priorityMap == null) return;

        // LOWEST -> LOW -> NORMAL -> HIGH -> HIGHEST -> MONITOR
        for (Event.Priority priority : Event.Priority.values()) {
            List<Consumer<Event>> handlersList = priorityMap.get(priority);
            if (handlersList != null) {
                handlersList.forEach(handler -> handler.accept(event));
            }
        }
    }

}
