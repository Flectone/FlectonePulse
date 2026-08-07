package net.flectone.pulse.platform.regitry;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.listener.message.PulseMessagePrepareListener;
import net.flectone.pulse.listener.message.PulseMessageSendListener;
import net.flectone.pulse.listener.player.PulsePlayerLoadListener;
import net.flectone.pulse.listener.player.PulsePlayerPersistAndDisposeListener;
import net.flectone.pulse.listener.proxy.cache.*;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.module.command.mute.listener.PulseMuteListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.util.logging.FLogger;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.UnaryOperator;
import net.flectone.pulse.exception.ListenerRegistrationException;

@Getter
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ListenerRegistryImpl implements ListenerRegistry {

    private final Map<Class<? extends Event>, EnumMap<Event.Priority, List<UnaryOperator<Event>>>> pulseListeners = new HashMap<>();
    private final Set<PulseListener> permanentListeners = new HashSet<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final ProxyRegistry proxyRegistry;
    private final FLogger fLogger;
    private final Injector injector;

    public @NonNull Map<Event.Priority, List<UnaryOperator<Event>>> getPulseListeners(Class<? extends Event> event) {
        lock.readLock().lock();
        try {
            Map<Event.Priority, List<UnaryOperator<Event>>> enumMap = pulseListeners.get(event);
            return enumMap == null ? Map.of() : Map.copyOf(enumMap);
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
            throw new ListenerRegistrationException("Class " + clazzListener.getName() + " is not a valid listener");
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
            throw new ListenerRegistrationException("@Pulse method must have single Event parameter " + method);
        }

        @SuppressWarnings("unchecked")
        Class<? extends Event> eventClass = (Class<? extends Event>) paramTypes[0];

        register(eventClass, annotation.priority(), event -> {
            if (event.cancelled() && !annotation.ignorecancelled()) return event;

            try {
                Object result = method.invoke(listener, event);
                return result instanceof Event newEvent ? newEvent : event;
            } catch (IllegalAccessException | InvocationTargetException e) {
                fLogger.warning(e, "Failed to invoke @Pulse handler");
                return event;
            }
        });
    }

    public void register(Class<? extends Event> eventClass, Event.Priority priority, UnaryOperator<Event> handler) {
        lock.writeLock().lock();
        try {
            pulseListeners
                    .computeIfAbsent(eventClass, _ -> new EnumMap<>(Event.Priority.class))
                    .computeIfAbsent(priority, _ -> new ArrayList<>())
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
    public void onDisable() {
        unregisterAll();
    }

    @Override
    public void onEnable() {
        registerDefaultListeners();
        lock.writeLock().lock();
        try {
            permanentListeners.forEach(this::register);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void registerDefaultListeners() {
        // need register here to format messages with mute from other plugins
        register(PulseMuteListener.class);

        register(PulseMessagePrepareListener.class);
        register(PulseMessageSendListener.class);
        register(PulsePlayerLoadListener.class);
        register(PulsePlayerPersistAndDisposeListener.class);

        if (proxyRegistry.hasEnabledProxy()) {
            register(BanCacheProxyMessageListener.class);
            register(ColorCacheProxyMessageListener.class);
            register(CooldownCacheProxyMessageListener.class);
            register(IgnoreCacheProxyMessageListener.class);
            register(KickCacheProxyMessageListener.class);
            register(MaintenanceCacheProxyMessageListener.class);
            register(MuteCacheProxyMessageListener.class);
            register(PlayerConnectedProxyMessageListener.class);
            register(PlayerDisconnectedProxyMessageListener.class);
            register(SettingCacheProxyMessageListener.class);
            register(SkinprofileCacheProxyMessageListener.class);
            register(ViolationCacheProxyMessageListener.class);
            register(WarnCacheProxyMessageListener.class);
            register(WhitelistCacheProxyMessageListener.class);
        }
    }
}