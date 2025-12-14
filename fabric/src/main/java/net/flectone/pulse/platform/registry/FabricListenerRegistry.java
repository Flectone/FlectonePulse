package net.flectone.pulse.platform.registry;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.mojang.brigadier.tree.CommandNode;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.flectone.pulse.FabricFlectonePulse;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.execution.scheduler.FabricTaskScheduler;
import net.flectone.pulse.listener.FabricBaseListener;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.TpsTracker;
import net.flectone.pulse.util.logging.FLogger;
import net.minecraft.commands.CommandSourceStack;

@Singleton
public class FabricListenerRegistry extends ListenerRegistry {

    private final Config config;
    private final FabricFlectonePulse fabricFlectonePulse;
    private final Provider<FabricBaseListener> fabricBaseListenerProvider;
    private final FabricTaskScheduler fabricTaskScheduler;
    private final TpsTracker tpsTracker;

    @Inject
    public FabricListenerRegistry(FileResolver fileResolver,
                                  FabricFlectonePulse fabricFlectonePulse,
                                  Provider<FabricBaseListener> fabricBaseListenerProvider,
                                  FabricTaskScheduler fabricTaskScheduler,
                                  TpsTracker tpsTracker,
                                  FLogger fLogger,
                                  Injector injector,
                                  PacketProvider packetProvider) {
        super(fLogger, injector, packetProvider);

        this.config = fileResolver.getConfig();
        this.fabricFlectonePulse = fabricFlectonePulse;
        this.fabricBaseListenerProvider = fabricBaseListenerProvider;
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

        FabricBaseListener fabricBaseListener = fabricBaseListenerProvider.get();
        ServerPlayConnectionEvents.JOIN.register(fabricBaseListener::asyncProcessJoinEvent);
        ServerPlayConnectionEvents.DISCONNECT.register(fabricBaseListener::asyncProcessQuitEvent);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            CommandNode<CommandSourceStack> root = dispatcher.getRoot();

            for (String command : config.getCommand().getDisabledFabric()) {
                root.getChildren().removeIf(node -> node.getName().equals(command));
            }
        });
    }
}
