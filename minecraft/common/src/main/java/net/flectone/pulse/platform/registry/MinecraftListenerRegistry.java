package net.flectone.pulse.platform.registry;

import com.github.retrooper.packetevents.event.EventManager;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import net.flectone.pulse.listener.BasePacketListener;
import net.flectone.pulse.listener.DialogPacketListener;
import net.flectone.pulse.listener.InventoryPacketListener;
import net.flectone.pulse.listener.MinecraftBasePulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.util.logging.FLogger;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class MinecraftListenerRegistry extends ListenerRegistry {

    private final List<PacketListenerCommon> packetListeners = new ArrayList<>();

    private final Injector injector;
    private final PacketProvider packetProvider;

    @Inject
    public MinecraftListenerRegistry(FLogger fLogger,
                                     Injector injector,
                                     PacketProvider packetProvider) {
        super(fLogger, injector);

        this.injector = injector;
        this.packetProvider = packetProvider;
    }

    public void registerDefaultListeners() {
        super.registerDefaultListeners();

        register(MinecraftBasePulseListener.class);
        register(BasePacketListener.class);
        register(InventoryPacketListener.class);

        if (packetProvider.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21_6)) {
            register(DialogPacketListener.class);
        }
    }

    @Override
    public void register(Class<?> clazzListener, Event.Priority eventPriority) {
        if (PacketListener.class.isAssignableFrom(clazzListener)) {
            PacketListener packetListener = (PacketListener) injector.getInstance(clazzListener);
            register(packetListener, PacketListenerPriority.valueOf(eventPriority.name()));
        } else {
            super.register(clazzListener, eventPriority);
        }
    }

    public void register(PacketListener packetListener, PacketListenerPriority priority) {
        PacketListenerCommon packetListenerCommon = packetProvider.getApi().getEventManager().registerListener(packetListener, priority);
        packetListeners.add(packetListenerCommon);
    }

    @Override
    public void unregisterAll() {
        EventManager eventManager = packetProvider.getApi().getEventManager();
        packetListeners.forEach(eventManager::unregisterListeners);
        packetListeners.clear();

        super.unregisterAll();
    }

}
