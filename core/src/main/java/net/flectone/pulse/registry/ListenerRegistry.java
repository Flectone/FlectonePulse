package net.flectone.pulse.registry;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import net.flectone.pulse.listener.BasePacketListener;
import net.flectone.pulse.listener.InventoryPacketListener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class ListenerRegistry implements Registry {

    private final List<PacketListenerCommon> packetListeners = new ArrayList<>();

    private final Injector injector;

    @Inject
    public ListenerRegistry(Injector injector) {
        this.injector = injector;
    }

    public void register(Class<? extends PacketListener> clazzListener) {
        register(clazzListener, PacketListenerPriority.NORMAL);
    }

    public void register(Class<? extends PacketListener> clazzListener, @NotNull PacketListenerPriority packetListenerPriority) {
        PacketListener packetListener = injector.getInstance(clazzListener);
        PacketListenerCommon packetListenerCommon = PacketEvents.getAPI().getEventManager().registerListener(packetListener, packetListenerPriority);
        packetListeners.add(packetListenerCommon);
    }

    public void unregisterAll() {
        packetListeners.forEach(listener -> PacketEvents.getAPI()
                .getEventManager()
                .unregisterListeners(listener)
        );
        packetListeners.clear();
    }

    @Override
    public void reload() {
        unregisterAll();
        registerDefaultListeners();
    }

    public void registerDefaultListeners() {
        register(InventoryPacketListener.class);
        register(BasePacketListener.class);
    }
}
