package net.flectone.pulse.manager;

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

public abstract class ListenerManager {

    private final List<PacketListenerCommon> packetListenerList = new ArrayList<>();

    protected final Injector injector;

    public ListenerManager(Injector injector) {
        this.injector = injector;
    }

    public void register(Class<? extends AbstractPacketListener> clazzListener) {
        register(clazzListener, PacketListenerPriority.NORMAL);
    }

    public void register(Class<? extends AbstractPacketListener> clazzListener, @NotNull PacketListenerPriority packetListenerPriority) {
        AbstractPacketListener abstractPacketListener = injector.getInstance(clazzListener);
        PacketListenerCommon packetListenerCommon = PacketEvents.getAPI().getEventManager().registerListener(abstractPacketListener, packetListenerPriority);
        packetListenerList.add(packetListenerCommon);
    }

    public void unregisterAll() {
        packetListenerList.forEach(listener -> PacketEvents.getAPI()
                .getEventManager()
                .unregisterListeners(listener)
        );
        packetListenerList.clear();
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
