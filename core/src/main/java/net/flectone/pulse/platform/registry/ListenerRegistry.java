package net.flectone.pulse.platform.registry;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.UnaryOperator;

@Getter
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ListenerRegistry implements Registry {

    private final Map<Class<? extends Event>, EnumMap<Event.Priority, List<UnaryOperator<Event>>>> pulseListeners = new Object2ObjectOpenHashMap<>();
    private final Set<PulseListener> permanentListeners = new ObjectOpenHashSet<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final FLogger fLogger;
    private final Injector injector;

    public @NonNull Map<Event.Priority, List<UnaryOperator<Event>>> getPulseListeners(Class<? extends Event> event) {
        lock.readLock().lock();
        try {
            Map<Event.Priority, List<UnaryOperator<Event>>> enumMap = pulseListeners.get(event);
            return enumMap == null ? Collections.emptyMap() : Collections.unmodifiableMap(enumMap);
        } finally {
            lock.readLock().unlock();
        }
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
                return result instanceof Event newEvent ? newEvent : event;
            } catch (IllegalAccessException | InvocationTargetException e) {
                fLogger.warning("Failed to invoke @Pulse handler", e);
                return event;
            }
        });
    }

    public void register(Class<? extends Event> eventClass, Event.Priority priority, UnaryOperator<Event> handler) {
        lock.writeLock().lock();
        try {
            pulseListeners
                    .computeIfAbsent(eventClass, k -> new EnumMap<>(Event.Priority.class))
                    .computeIfAbsent(priority, k -> new ObjectArrayList<>())
                    .add(handler);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void unregisterAll() {
        lock.writeLock().lock();
        try {
            pulseListeners.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void reload() {
        unregisterAll();
        registerDefaultListeners();
        lock.writeLock().lock();
        try {
            permanentListeners.forEach(this::register);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void registerDefaultListeners() {
        register(BasePulseListener.class);
        register(MessagePulseListener.class);
        register(MutePulseListener.class);
    }
}