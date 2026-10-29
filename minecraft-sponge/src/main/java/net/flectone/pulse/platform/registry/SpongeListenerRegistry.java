package net.flectone.pulse.platform.registry;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import net.flectone.pulse.SpongeFlectonePulse;
import net.flectone.pulse.listener.player.SpongePlayerConnectionListener;
import net.flectone.pulse.listener.player.SpongePlayerLoginListener;
import net.flectone.pulse.logging.FLogger;
import net.flectone.pulse.platform.provider.MinecraftPacketProvider;
import net.flectone.pulse.util.LazyInstance;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.plugin.PluginContainer;

import java.lang.invoke.MethodHandles;

@Singleton
public class SpongeListenerRegistry extends MinecraftListenerRegistry {

    private final SpongeFlectonePulse spongeFlectonePulse;
    private final LazyInstance<SpongePlayerConnectionListener> spongePlayerConnectionListener;
    private final LazyInstance<SpongePlayerLoginListener> spongePlayerLoginListener;

    private boolean spongeListenersRegistered;

    @Inject
    public SpongeListenerRegistry(ProxyRegistry proxyRegistry,
                                  SpongeFlectonePulse spongeFlectonePulse,
                                  LazyInstance<SpongePlayerConnectionListener> spongePlayerConnectionListener,
                                  LazyInstance<SpongePlayerLoginListener> spongePlayerLoginListener,
                                  FLogger fLogger,
                                  Injector injector,
                                  MinecraftPacketProvider packetProvider) {
        super(proxyRegistry, fLogger, injector, packetProvider);

        this.spongeFlectonePulse = spongeFlectonePulse;
        this.spongePlayerConnectionListener = spongePlayerConnectionListener;
        this.spongePlayerLoginListener = spongePlayerLoginListener;
    }

    @Override
    public void registerDefaultListeners() {
        super.registerDefaultListeners();

        // skip double register
        if (spongeListenersRegistered) return;

        PluginContainer container = spongeFlectonePulse.getContainer();
        Sponge.eventManager().registerListeners(container, this, MethodHandles.lookup());
        Sponge.eventManager().registerListeners(container, spongePlayerLoginListener.get(), MethodHandles.lookup());
        Sponge.eventManager().registerListeners(container, spongePlayerConnectionListener.get(), MethodHandles.lookup());

        spongeListenersRegistered = true;
    }

    @Listener
    public void onStartedEngineEvent(StartedEngineEvent<Server> event) {
        spongeFlectonePulse.setMinecraftServer((MinecraftServer) event.engine());
    }
}
