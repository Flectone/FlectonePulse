package net.flectone.pulse.platform.registry;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.event.IBaseEvent;
import com.hypixel.hytale.server.core.io.adapter.PacketAdapters;
import com.hypixel.hytale.server.core.io.adapter.PlayerPacketFilter;
import com.hypixel.hytale.server.core.io.adapter.PlayerPacketWatcher;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import net.flectone.pulse.listener.HytaleBaseListener;
import net.flectone.pulse.listener.HytaleListener;
import net.flectone.pulse.util.logging.FLogger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Singleton
public class HytaleListenerRegistry extends ListenerRegistry {

    // don't clear these listeners
    private final List<HytaleListener> listeners = new ArrayList<>();

    private final FLogger fLogger;
    private final Injector injector;
    private final EventRegistry eventRegistry;

    private boolean basePacketsRegistered;

    @Inject
    public HytaleListenerRegistry(FLogger fLogger,
                                  Injector injector,
                                  JavaPlugin javaPlugin) {
        super(fLogger, injector);

        this.fLogger = fLogger;
        this.injector = injector;
        this.eventRegistry = javaPlugin.getEventRegistry();
    }

    @Override
    public void registerDefaultListeners() {
        super.registerDefaultListeners();

        register(HytaleBaseListener.class);

        if (!basePacketsRegistered) {
            HytaleBaseListener hytaleBaseListener = injector.getInstance(HytaleBaseListener.class);
            registerInboundWatcher(hytaleBaseListener.createDisconnectWatcher());
            registerInboundWatcher(hytaleBaseListener.createUpdateLanguageWatcher());
            basePacketsRegistered = true;
        }
    }

    public void registerOutboundFilter(PlayerPacketFilter packetFilter) {
        PacketAdapters.registerOutbound(packetFilter);
    }

    public void registerOutboundWatcher(PlayerPacketWatcher packetWatcher) {
        PacketAdapters.registerOutbound(packetWatcher);
    }

    public void registerInboundFilter(PlayerPacketFilter packetFilter) {
        PacketAdapters.registerInbound(packetFilter);
    }

    public void registerInboundWatcher(PlayerPacketWatcher packetWatcher) {
        PacketAdapters.registerInbound(packetWatcher);
    }

    @Override
    public void register(Class<?> clazzListener, net.flectone.pulse.model.event.Event.Priority eventPriority) {
        if (HytaleListener.class.isAssignableFrom(clazzListener)) {
            HytaleListener bukkitListener = (HytaleListener) injector.getInstance(clazzListener);
            register(bukkitListener, switch (eventPriority) {
                case LOWEST -> EventPriority.FIRST;
                case LOW -> EventPriority.EARLY;
                case NORMAL -> EventPriority.NORMAL;
                case HIGH -> EventPriority.LATE;
                case HIGHEST, MONITOR -> EventPriority.LAST;
            });

            return;
        }

        super.register(clazzListener, eventPriority);
    }

    public void register(HytaleListener hytaleListener, EventPriority eventPriority) {
        // don't register HytaleListener a second time when reloading
        // because Hytale doesn't support removing Listeners in runtime
        if (listeners.contains(hytaleListener)) return;

        listeners.add(hytaleListener);
        registerEvents(hytaleListener, eventPriority);
    }

    @SuppressWarnings("unchecked")
    private void registerEvents(HytaleListener hytaleListener, EventPriority eventPriority) {
        for (Method method : hytaleListener.getClass().getMethods()) {
            if (method.isBridge() || method.isSynthetic()) continue;

            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length != 1) continue;

            Class<?> eventClass = parameterTypes[0];
            if (!IBaseEvent.class.isAssignableFrom(eventClass)) continue;

            method.setAccessible(true);

            @SuppressWarnings("rawtypes")
            Consumer consumer = event -> {
                try {
                    method.invoke(hytaleListener, event);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    fLogger.warning("Error invoking event handler " + method.getName(), e);
                }
            };

            eventRegistry.registerGlobal(eventPriority, eventClass, consumer);
        }
    }

}
