package net.flectone.pulse.platform.registry;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import net.flectone.pulse.listener.BukkitBaseListener;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.util.logging.FLogger;
import org.bukkit.event.*;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class BukkitListenerRegistry extends ListenerRegistry {

    private final List<Listener> listeners = new ArrayList<>();
    private final Plugin plugin;
    private final Injector injector;

    @Inject
    public BukkitListenerRegistry(Plugin plugin,
                                  FLogger fLogger,
                                  Injector injector,
                                  PacketProvider packetProvider) {
        super(fLogger, injector, packetProvider);

        this.plugin = plugin;
        this.injector = injector;
    }

    @Override
    public void register(Class<?> clazzListener, net.flectone.pulse.model.event.Event.Priority eventPriority) {
        if (Listener.class.isAssignableFrom(clazzListener)) {
            Listener bukkitListener = (Listener) injector.getInstance(clazzListener);
            register(bukkitListener, EventPriority.valueOf(eventPriority.name()));
            return;
        }

        super.register(clazzListener, eventPriority);
    }

    public void register(Listener bukkitListener, EventPriority eventPriority) {
        listeners.add(bukkitListener);
        registerEvents(bukkitListener, eventPriority);
    }

    private void registerEvents(Listener abstractListener, EventPriority eventPriority) {
        PluginManager pluginManager = plugin.getServer().getPluginManager();

        for (Method method : abstractListener.getClass().getMethods()) {
            EventHandler eventHandler = method.getAnnotation(EventHandler.class);
            if (eventHandler == null) continue;

            if (method.isBridge() || method.isSynthetic()) {
                continue;
            }

            Class<? extends Event> eventClass = method.getParameterTypes()[0].asSubclass(Event.class);
            method.setAccessible(true);

            EventExecutor executor = (listener, event) -> {
                try {
                    if (!eventClass.isAssignableFrom(event.getClass())) {
                        return;
                    }
                    method.invoke(listener, event);
                } catch (InvocationTargetException ex) {
                    throw new EventException(ex.getCause());
                } catch (Throwable t) {
                    throw new EventException(t);
                }
            };

            pluginManager.registerEvent(eventClass, abstractListener, eventPriority, executor, plugin, false);
        }
    }

    @Override
    public void unregisterAll() {
        super.unregisterAll();

        listeners.forEach(HandlerList::unregisterAll);
        listeners.clear();
    }

    @Override
    public void registerDefaultListeners() {
        super.registerDefaultListeners();

        register(BukkitBaseListener.class, net.flectone.pulse.model.event.Event.Priority.LOWEST);
    }
}
