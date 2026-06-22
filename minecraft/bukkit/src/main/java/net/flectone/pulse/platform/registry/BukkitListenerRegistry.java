package net.flectone.pulse.platform.registry;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.flectone.pulse.listener.player.BukkitPlayerConnectionListener;
import net.flectone.pulse.listener.player.BukkitPlayerConnectionValidListener;
import net.flectone.pulse.listener.player.BukkitPlayerLoginListener;
import net.flectone.pulse.listener.player.PaperPlayerLoginListener;
import net.flectone.pulse.platform.provider.MinecraftPacketProvider;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.util.logging.FLogger;
import org.bukkit.event.*;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

@Singleton
public class BukkitListenerRegistry extends MinecraftListenerRegistry {

    private final List<Listener> listeners = new ObjectArrayList<>();

    private final Plugin plugin;
    private final Injector injector;
    private final ReflectionResolver reflectionResolver;

    @Inject
    public BukkitListenerRegistry(ProxyRegistry proxyRegistry,
                                  Plugin plugin,
                                  FLogger fLogger,
                                  Injector injector,
                                  MinecraftPacketProvider packetProvider,
                                  ReflectionResolver reflectionResolver) {
        super(proxyRegistry, fLogger, injector, packetProvider);

        this.plugin = plugin;
        this.injector = injector;
        this.reflectionResolver = reflectionResolver;
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
                } catch (InvocationTargetException e) {
                    throw new EventException(e.getCause());
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

        if (reflectionResolver.hasMethod(AsyncPlayerPreLoginEvent.class, "kickMessage")) {
            register(PaperPlayerLoginListener.class, net.flectone.pulse.model.event.Event.Priority.LOWEST);
        } else {
            register(BukkitPlayerLoginListener.class, net.flectone.pulse.model.event.Event.Priority.LOWEST);
        }

        register(BukkitPlayerConnectionListener.class, net.flectone.pulse.model.event.Event.Priority.LOWEST);
        register(BukkitPlayerConnectionValidListener.class, net.flectone.pulse.model.event.Event.Priority.MONITOR);
    }
}
