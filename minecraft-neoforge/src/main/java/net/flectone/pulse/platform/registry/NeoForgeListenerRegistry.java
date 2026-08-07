package net.flectone.pulse.platform.registry;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import net.flectone.pulse.NeoForgeFlectonePulse;
import net.flectone.pulse.listener.player.NeoForgePlayerConnectionListener;
import net.flectone.pulse.listener.player.NeoForgePlayerLoginListener;
import net.flectone.pulse.logging.FLogger;
import net.flectone.pulse.platform.provider.MinecraftPacketProvider;
import net.flectone.pulse.util.LazyInstance;
import net.flectone.pulse.util.NeoForgeTpsTracker;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Singleton
public class NeoForgeListenerRegistry extends MinecraftListenerRegistry {

    private final NeoForgeFlectonePulse neoForgeFlectonePulse;
    private final LazyInstance<NeoForgePlayerConnectionListener> neoForgePlayerConnectionListener;
    private final LazyInstance<NeoForgePlayerLoginListener> neoForgePlayerLoginListener;
    private final NeoForgeTpsTracker tpsTracker;

    @Inject
    public NeoForgeListenerRegistry(ProxyRegistry proxyRegistry,
                                    NeoForgeFlectonePulse neoForgeFlectonePulse,
                                    LazyInstance<NeoForgePlayerConnectionListener> neoForgePlayerConnectionListener,
                                    LazyInstance<NeoForgePlayerLoginListener> neoForgePlayerLoginListener,
                                    NeoForgeTpsTracker tpsTracker,
                                    FLogger fLogger,
                                    Injector injector,
                                    MinecraftPacketProvider packetProvider) {
        super(proxyRegistry, fLogger, injector, packetProvider);

        this.neoForgeFlectonePulse = neoForgeFlectonePulse;
        this.neoForgePlayerConnectionListener = neoForgePlayerConnectionListener;
        this.neoForgePlayerLoginListener = neoForgePlayerLoginListener;
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
        NeoForgePlayerLoginListener loginListener = neoForgePlayerLoginListener.get();
        neoForgeFlectonePulse.getLoader().getEventBus().addListener(loginListener::onPreLogin);

        // register connection listener
        NeoForgePlayerConnectionListener connectionListener = neoForgePlayerConnectionListener.get();
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) ->
                connectionListener.asyncProcessJoinEvent(((ServerPlayer) event.getEntity()).connection)
        );
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event) ->
                connectionListener.asyncProcessQuitEvent(((ServerPlayer) event.getEntity()).connection)
        );
    }
}