package net.flectone.pulse.platform.registry;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import net.flectone.pulse.NeoForgeFlectonePulse;
import net.flectone.pulse.listener.player.NeoForgePlayerConnectionListener;
import net.flectone.pulse.listener.player.NeoForgePlayerLoginListener;
import net.flectone.pulse.platform.provider.MinecraftPacketProvider;
import net.flectone.pulse.util.NeoForgeTpsTracker;
import net.flectone.pulse.util.logging.FLogger;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Singleton
public class NeoForgeListenerRegistry extends MinecraftListenerRegistry {

    private final NeoForgeFlectonePulse neoForgeFlectonePulse;
    private final Provider<NeoForgePlayerConnectionListener> neoForgePlayerConnectionListenerProvider;
    private final Provider<NeoForgePlayerLoginListener> neoForgePlayerLoginListenerProvider;
    private final NeoForgeTpsTracker tpsTracker;

    @Inject
    public NeoForgeListenerRegistry(ProxyRegistry proxyRegistry,
                                    NeoForgeFlectonePulse neoForgeFlectonePulse,
                                    Provider<NeoForgePlayerConnectionListener> neoForgePlayerConnectionListenerProvider,
                                    Provider<NeoForgePlayerLoginListener> neoForgePlayerLoginListenerProvider,
                                    NeoForgeTpsTracker tpsTracker,
                                    FLogger fLogger,
                                    Injector injector,
                                    MinecraftPacketProvider packetProvider) {
        super(proxyRegistry, fLogger, injector, packetProvider);

        this.neoForgeFlectonePulse = neoForgeFlectonePulse;
        this.neoForgePlayerConnectionListenerProvider = neoForgePlayerConnectionListenerProvider;
        this.neoForgePlayerLoginListenerProvider = neoForgePlayerLoginListenerProvider;
        this.tpsTracker = tpsTracker;
    }

    @Override
    public void registerDefaultListeners() {
        super.registerDefaultListeners();

        // skip double register
        if (neoForgeFlectonePulse.getMinecraftServer() != null) return;

        NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class, _ -> tpsTracker.onTick());
        NeoForge.EVENT_BUS.addListener((ServerStartedEvent event) -> neoForgeFlectonePulse.setMinecraftServer(event.getServer()));
        NeoForge.EVENT_BUS.addListener((ServerStoppingEvent _) -> neoForgeFlectonePulse.onDisable());

        // register pre login listener
        NeoForgePlayerLoginListener loginListener = neoForgePlayerLoginListenerProvider.get();
        neoForgeFlectonePulse.getModEventBus().addListener(loginListener::onPreLogin);

        // register connection listener
        NeoForgePlayerConnectionListener connectionListener = neoForgePlayerConnectionListenerProvider.get();
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) ->
                connectionListener.asyncProcessJoinEvent(((ServerPlayer) event.getEntity()).connection)
        );
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event) ->
                connectionListener.asyncProcessQuitEvent(((ServerPlayer) event.getEntity()).connection)
        );
    }
}
