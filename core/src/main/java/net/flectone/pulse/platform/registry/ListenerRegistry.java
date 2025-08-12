package net.flectone.pulse.platform.registry;

import com.github.retrooper.packetevents.event.EventManager;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.*;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.util.logging.FLogger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

@Getter
@Singleton
public class ListenerRegistry implements Registry {

    private final Map<Class<? extends Event>, EnumMap<Event.Priority, List<Consumer<Event>>>> pulseListeners = new ConcurrentHashMap<>();
    private final List<PacketListenerCommon> packetListeners = new ArrayList<>();

    private final FLogger fLogger;
    private final Injector injector;
    private final PacketProvider packetProvider;

    @Inject
    public ListenerRegistry(FLogger fLogger,
                            Injector injector,
                            PacketProvider packetProvider) {
        this.fLogger = fLogger;
        this.injector = injector;
        this.packetProvider = packetProvider;
    }

    public void register(Class<?> clazzListener) {
        register(clazzListener, Event.Priority.NORMAL);
    }

    public void register(Class<?> clazzListener, Event.Priority eventPriority) {
        if (PacketListener.class.isAssignableFrom(clazzListener)) {
            PacketListener packetListener = (PacketListener) injector.getInstance(clazzListener);
            register(packetListener, PacketListenerPriority.valueOf(eventPriority.name()));
            return;
        }

        if (PulseListener.class.isAssignableFrom(clazzListener)) {
            PulseListener pulseListener = (PulseListener) injector.getInstance(clazzListener);
            register(pulseListener);
        }
    }

    public void register(PacketListener packetListener, PacketListenerPriority priority) {
        PacketListenerCommon packetListenerCommon = packetProvider.getEventManager().registerListener(packetListener, priority);
        packetListeners.add(packetListenerCommon);
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
            if (event.isCancelled() && !annotation.ignoreCancelled()) return;

            try {
                method.invoke(listener, event);
            } catch (IllegalAccessException | InvocationTargetException e) {
                fLogger.warning("Failed to invoke @Pulse handler");
                fLogger.warning(e);
            }
        });
    }

    public void register(Class<? extends Event> eventClass, Event.Priority priority, Consumer<Event> handler) {
        pulseListeners.computeIfAbsent(eventClass, k -> new EnumMap<>(Event.Priority.class))
                .computeIfAbsent(priority, k -> new CopyOnWriteArrayList<>())
                .add(handler);
    }

    public void unregisterAll() {
        EventManager eventManager = packetProvider.getEventManager();
        packetListeners.forEach(eventManager::unregisterListeners);
        packetListeners.clear();
        pulseListeners.clear();
    }

    @Override
    public void reload() {
        unregisterAll();
        registerDefaultListeners();
    }

    public void registerDefaultListeners() {
        register(BasePacketListener.class);
        register(FPlayerPulseListener.class);
        register(LegacyMiniConvertorPulseListener.class);
        register(MessagePulseListener.class);

        register(InventoryPacketListener.class);

        if (packetProvider.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21_6)) {
            register(DialogPacketListener.class);
        }
    }
}
