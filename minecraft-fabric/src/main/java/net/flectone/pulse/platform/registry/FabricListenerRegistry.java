package net.flectone.pulse.platform.registry;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.flectone.pulse.FabricFlectonePulse;
import net.flectone.pulse.listener.player.FabricPlayerConnectionListener;
import net.flectone.pulse.platform.provider.MinecraftPacketProvider;
import net.flectone.pulse.util.FabricTpsTracker;
import net.flectone.pulse.util.LazyInstance;
import net.flectone.pulse.util.logging.FLogger;

@Singleton
public class FabricListenerRegistry extends MinecraftListenerRegistry {

    private final FabricFlectonePulse fabricFlectonePulse;
    private final LazyInstance<FabricPlayerConnectionListener> fabricBaseListener;
    private final FabricTpsTracker tpsTracker;

    @Inject
    public FabricListenerRegistry(ProxyRegistry proxyRegistry,
                                  FabricFlectonePulse fabricFlectonePulse,
                                  LazyInstance<FabricPlayerConnectionListener> fabricBaseListener,
                                  FabricTpsTracker tpsTracker,
                                  FLogger fLogger,
                                  Injector injector,
                                  MinecraftPacketProvider packetProvider) {
        super(proxyRegistry, fLogger, injector, packetProvider);

        this.fabricFlectonePulse = fabricFlectonePulse;
        this.fabricBaseListener = fabricBaseListener;
        this.tpsTracker = tpsTracker;
    }

    @Override
    public void registerDefaultListeners() {
        super.registerDefaultListeners();

        // skip double register
        if (fabricFlectonePulse.getMinecraftServer() != null) return;

        ServerTickEvents.END_SERVER_TICK.register(_ -> tpsTracker.onTick());
        ServerLifecycleEvents.SERVER_STARTING.register(fabricFlectonePulse::setMinecraftServer);

        // register connection listener
        FabricPlayerConnectionListener fabricPlayerConnectionListenerInstance = fabricBaseListener.get();
        ServerPlayConnectionEvents.JOIN.register(fabricPlayerConnectionListenerInstance::asyncProcessJoinEvent);
        ServerPlayConnectionEvents.DISCONNECT.register(fabricPlayerConnectionListenerInstance::asyncProcessQuitEvent);
    }
}