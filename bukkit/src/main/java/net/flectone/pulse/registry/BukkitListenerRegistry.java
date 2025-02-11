package net.flectone.pulse.registry;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
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
    public BukkitListenerRegistry(Plugin plugin, Injector injector) {
        super(injector);

        this.plugin = plugin;
        this.injector = injector;
    }

    public void register(Class<? extends Listener> clazzListener, EventPriority eventPriority) {
        Listener abstractListener = injector.getInstance(clazzListener);
        listeners.add(abstractListener);
        registerEvents(abstractListener, eventPriority);
    }

    private void registerEvents(Listener abstractListener, EventPriority eventPriority) {
        PluginManager pluginManager = plugin.getServer().getPluginManager();

        for (final Method method : abstractListener.getClass().getMethods()) {
            final EventHandler eventHandler = method.getAnnotation(EventHandler.class);
            if (eventHandler == null) continue;

            if (method.isBridge() || method.isSynthetic()) {
                continue;
            }

            final Class<? extends Event> eventClass = method.getParameterTypes()[0].asSubclass(Event.class);
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
}
