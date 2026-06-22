package net.flectone.pulse.platform.registry;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.event.IBaseEvent;
import com.hypixel.hytale.protocol.packets.interface_.UpdateLanguage;
import com.hypixel.hytale.server.core.io.adapter.PacketAdapters;
import com.hypixel.hytale.server.core.io.adapter.PlayerPacketFilter;
import com.hypixel.hytale.server.core.io.adapter.PlayerPacketWatcher;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.listener.HytaleListener;
import net.flectone.pulse.listener.module.HytalePulseModuleEnableListener;
import net.flectone.pulse.listener.player.HytalePlayerConnectionListener;
import net.flectone.pulse.listener.player.HytalePlayerLoginListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Singleton
public class HytaleListenerRegistry extends ListenerRegistry {

    // don't clear these listeners
    private final List<HytaleListener> listeners = new ObjectArrayList<>();
    private final AtomicBoolean languageListenerRegistered = new AtomicBoolean(false);

    private final FileFacade fileFacade;
    private final FLogger fLogger;
    private final Injector injector;
    private final JavaPlugin javaPlugin;
    private final EventRegistry eventRegistry;
    private final TaskScheduler taskScheduler;
    private final Provider<FPlayerService> fPlayerServiceProvider;
    private final Provider<SocialService> socialServiceProvider;

    @Inject
    public HytaleListenerRegistry(ProxyRegistry proxyRegistry,
                                  FileFacade fileFacade,
                                  FLogger fLogger,
                                  Injector injector,
                                  JavaPlugin javaPlugin,
                                  TaskScheduler taskScheduler,
                                  Provider<FPlayerService> fPlayerServiceProvider,
                                  Provider<SocialService> socialServiceProvider) {
        super(proxyRegistry, fLogger, injector);

        this.fileFacade = fileFacade;
        this.fLogger = fLogger;
        this.injector = injector;
        this.javaPlugin = javaPlugin;
        this.eventRegistry = javaPlugin.getEventRegistry();
        this.taskScheduler = taskScheduler;
        this.fPlayerServiceProvider = fPlayerServiceProvider;
        this.socialServiceProvider = socialServiceProvider;
    }

    @Override
    public void registerDefaultListeners() {
        super.registerDefaultListeners();

        register(HytalePlayerLoginListener.class);
        register(HytalePlayerConnectionListener.class);
        register(HytalePulseModuleEnableListener.class);

        // Hytale has no way to remove packet listener
        if (languageListenerRegistered.compareAndSet(false, true)) {
            registerInboundWatcher((playerRef, packet) -> {
                if (packet instanceof UpdateLanguage updateLanguage) {
                    String language = StringUtils.isEmpty(updateLanguage.language)
                            ? fileFacade.config().language().type().toLowerCase(Locale.ROOT)
                            : Strings.CS.replace(updateLanguage.language.toLowerCase(Locale.ROOT), "-", "_");
                    taskScheduler.runAsync(() -> {
                        FPlayer fPlayer = fPlayerServiceProvider.get().getFPlayer(playerRef.getUuid());

                        socialServiceProvider.get().updateLocale(fPlayer, language);
                    });
                }
            });
        }
    }

    public void register(Consumer<JavaPlugin> javaPluginConsumer) {
        javaPluginConsumer.accept(javaPlugin);
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
                    fLogger.warning(e, "Error invoking event handler %s", method.getName());
                }
            };

            eventRegistry.registerGlobal(eventPriority, eventClass, consumer);
        }
    }

}
