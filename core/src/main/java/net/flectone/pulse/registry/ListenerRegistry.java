package net.flectone.pulse.registry;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.google.inject.Injector;
import net.flectone.pulse.listener.AbstractPacketListener;
import net.flectone.pulse.listener.BasePacketListener;
import net.flectone.pulse.listener.FInventoryPacketListener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class ListenerRegistry {

    private final List<PacketListenerCommon> packetListeners = new ArrayList<>();

    private final Injector injector;

    public ListenerRegistry(Injector injector) {
        this.injector = injector;
    }

    public void register(Class<? extends AbstractPacketListener> clazzListener) {
        register(clazzListener, PacketListenerPriority.NORMAL);
    }

    public void register(Class<? extends AbstractPacketListener> clazzListener, @NotNull PacketListenerPriority packetListenerPriority) {
        AbstractPacketListener abstractPacketListener = injector.getInstance(clazzListener);
        PacketListenerCommon packetListenerCommon = PacketEvents.getAPI().getEventManager().registerListener(abstractPacketListener, packetListenerPriority);
        packetListeners.add(packetListenerCommon);
    }

    public void unregisterAll() {
        packetListeners.forEach(listener -> PacketEvents.getAPI()
                .getEventManager()
                .unregisterListeners(listener)
        );
        packetListeners.clear();
    }

    public void reload() {
        unregisterAll();
        registerDefaultListeners();
    }

    public void registerDefaultListeners() {
        register(FInventoryPacketListener.class);
        register(BasePacketListener.class);
    }
}
