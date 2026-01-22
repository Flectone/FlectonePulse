package net.flectone.pulse.platform.registry;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.BasePulseListener;
import net.flectone.pulse.listener.MessagePulseListener;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.module.command.mute.listener.MutePulseListener;
import net.flectone.pulse.util.logging.FLogger;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.UnaryOperator;

@Getter
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ListenerRegistry implements Registry {

    private final Map<Class<? extends Event>, EnumMap<Event.Priority, List<UnaryOperator<Event>>>> pulseListeners = new ConcurrentHashMap<>();
    private final Set<PulseListener> permanentListeners = new HashSet<>();

    private final FLogger fLogger;
    private final Injector injector;

    public @NonNull Map<Event.Priority, List<UnaryOperator<Event>>> getPulseListeners(Class<? extends Event> event) {
        EnumMap<Event.Priority, List<UnaryOperator<Event>>> enumMap = pulseListeners.get(event);
        if (enumMap != null) return new EnumMap<>(enumMap);

        return new EnumMap<>(Event.Priority.class);
    }

    public void registerPermanent(PulseListener pulseListener) {
        permanentListeners.add(pulseListener);
        register(pulseListener);
    }

    public void register(Class<?> clazzListener) {
        register(clazzListener, Event.Priority.NORMAL);
    }

    public void register(Class<?> clazzListener, Event.Priority eventPriority) {
        if (PulseListener.class.isAssignableFrom(clazzListener)) {
            PulseListener pulseListener = (PulseListener) injector.getInstance(clazzListener);
            register(pulseListener);
        } else {
            throw new IllegalArgumentException("Class " + clazzListener.getName() + " is not a valid listener");
        }
    }

    public void register(PulseListener pulseListener) {
        for (Method method : pulseListener.getClass().getMethods()) {
            if (method.isAnnotationPresent(Pulse.class)) {
                if (method.isBridge() || method.isSynthetic()) continue;

                Pulse annotation = method.getAnnotation(Pulse.class);
                registerAnnotatedMethod(pulseListener, method, annotation);
            }
        }
    }

    private void registerAnnotatedMethod(PulseListener listener, Method method, Pulse annotation) {
        Class<?>[] paramTypes = method.getParameterTypes();
        if (paramTypes.length != 1 || !Event.class.isAssignableFrom(paramTypes[0])) {
            throw new IllegalArgumentException("@Pulse method must have single Event parameter: " + method);
        }

        @SuppressWarnings("unchecked")
        Class<? extends Event> eventClass = (Class<? extends Event>) paramTypes[0];

        register(eventClass, annotation.priority(), event -> {
            if (event.cancelled() && !annotation.ignoreCancelled()) return event;

            try {
                Object result = method.invoke(listener, event);

                if (result instanceof Event newEvent) {
                    return newEvent;
                }

                return event;
            } catch (IllegalAccessException | InvocationTargetException e) {
                fLogger.warning("Failed to invoke @Pulse handler");
                fLogger.warning(e);
                return event;
            }
        });
    }

    public void register(Class<? extends Event> eventClass, Event.Priority priority, UnaryOperator<Event> handler) {
        pulseListeners.computeIfAbsent(eventClass, k -> new EnumMap<>(Event.Priority.class))
                .computeIfAbsent(priority, k -> new CopyOnWriteArrayList<>())
                .add(handler);
    }

    public void unregisterAll() {
        pulseListeners.clear();
    }

    @Override
    public void reload() {
        unregisterAll();
        registerDefaultListeners();
        permanentListeners.forEach(this::register);
    }

    public void registerDefaultListeners() {
        register(BasePulseListener.class);
        register(MessagePulseListener.class);
        register(MutePulseListener.class);
    }

}