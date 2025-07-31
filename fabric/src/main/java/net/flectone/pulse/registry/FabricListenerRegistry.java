package net.flectone.pulse.registry;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.mojang.brigadier.tree.CommandNode;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.flectone.pulse.FabricFlectonePulse;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.configuration.Config;
import net.flectone.pulse.dispatcher.EventDispatcher;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.event.player.PlayerLoadEvent;
import net.flectone.pulse.model.event.player.PlayerPersistAndDisposeEvent;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.scheduler.FabricTaskScheduler;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.TpsTracker;
import net.flectone.pulse.util.logging.FLogger;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.*;

@Singleton
public class FabricListenerRegistry extends ListenerRegistry {

    private final Config config;
    private final FabricFlectonePulse fabricFlectonePulse;
    private final FPlayerService fPlayerService;
    private final EventDispatcher eventDispatcher;
    private final FabricTaskScheduler fabricTaskScheduler;
    private final TpsTracker tpsTracker;

    @Inject
    public FabricListenerRegistry(FileResolver fileResolver,
                                  FabricFlectonePulse fabricFlectonePulse,
                                  FPlayerService fPlayerService,
                                  EventDispatcher eventDispatcher,
                                  FabricTaskScheduler fabricTaskScheduler,
                                  TpsTracker tpsTracker,
                                  FLogger fLogger,
                                  Injector injector) {
        super(fLogger, injector);

        this.config = fileResolver.getConfig();
        this.fabricFlectonePulse = fabricFlectonePulse;
        this.fPlayerService = fPlayerService;
        this.eventDispatcher = eventDispatcher;
        this.fabricTaskScheduler = fabricTaskScheduler;
        this.tpsTracker = tpsTracker;
    }

    @Override
    public void registerDefaultListeners() {
        super.registerDefaultListeners();

        // skip double register
        if (fabricFlectonePulse.getMinecraftServer() != null) return;

        ServerTickEvents.START_SERVER_TICK.register(server -> fabricTaskScheduler.onTick());
        ServerTickEvents.END_SERVER_TICK.register(server -> tpsTracker.onTick());
        ServerLifecycleEvents.SERVER_STARTING.register(fabricFlectonePulse::setMinecraftServer);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> fabricFlectonePulse.onDisable());

        ServerPlayConnectionEvents.JOIN.register((handler, packetSender, minecraftServer) -> {
            ServerPlayerEntity player = handler.getPlayer();
            asyncProcessJoinEvent(player.getUuid());
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            asyncProcessQuitEvent(player.getUuid());
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            CommandNode<ServerCommandSource> root = dispatcher.getRoot();

            for (String command : config.getFabricDisabledCommands()) {
                root.getChildren().removeIf(node -> node.getName().equals(command));
            }
        });
    }

    @Async
    public void asyncProcessJoinEvent(UUID uuid) {
        FPlayer fPlayer = fPlayerService.getFPlayer(uuid);

        eventDispatcher.dispatch(new PlayerLoadEvent(fPlayer));
        eventDispatcher.dispatch(new net.flectone.pulse.model.event.player.PlayerJoinEvent(fPlayer));
    }

    @Async
    public void asyncProcessQuitEvent(UUID uuid) {
        FPlayer fPlayer = fPlayerService.getFPlayer(uuid);

        eventDispatcher.dispatch(new net.flectone.pulse.model.event.player.PlayerQuitEvent(fPlayer));
        eventDispatcher.dispatch(new PlayerPersistAndDisposeEvent(fPlayer));
    }
}
