package net.flectone.pulse.platform.registry;

import com.github.retrooper.packetevents.event.EventManager;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.flectone.pulse.listener.MinecraftBasePacketListener;
import net.flectone.pulse.listener.MinecraftPacketDialogListener;
import net.flectone.pulse.listener.MinecraftPacketInventoryListener;
import net.flectone.pulse.listener.MinecraftPulseBaseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.platform.provider.MinecraftPacketProvider;
import net.flectone.pulse.util.logging.FLogger;

import java.util.List;

@Singleton
public class MinecraftListenerRegistry extends ListenerRegistry {

    private final List<PacketListenerCommon> packetListeners = new ObjectArrayList<>();

    private final Injector injector;
    private final MinecraftPacketProvider packetProvider;

    @Inject
    public MinecraftListenerRegistry(FLogger fLogger,
                                     Injector injector,
                                     MinecraftPacketProvider packetProvider) {
        super(fLogger, injector);

        this.injector = injector;
        this.packetProvider = packetProvider;
    }

    @Override
    public void registerDefaultListeners() {
        super.registerDefaultListeners();

        register(MinecraftPulseBaseListener.class);
        register(MinecraftBasePacketListener.class);
        register(MinecraftPacketInventoryListener.class);

        if (packetProvider.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21_6)) {
            register(MinecraftPacketDialogListener.class);
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
