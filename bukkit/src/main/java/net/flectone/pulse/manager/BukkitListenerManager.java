package net.flectone.pulse.manager;

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
public class BukkitListenerManager extends ListenerManager {

    private final List<Listener> listenerList = new ArrayList<>();
    private final Plugin plugin;

    @Inject
    public BukkitListenerManager(Plugin plugin, Injector injector) {
        super(injector);

        this.plugin = plugin;
    }

    public void register(Class<? extends Listener> clazzListener, EventPriority eventPriority) {
        Listener abstractListener = injector.getInstance(clazzListener);
        listenerList.add(abstractListener);
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

        listenerList.forEach(HandlerList::unregisterAll);
        listenerList.clear();
    }
}
