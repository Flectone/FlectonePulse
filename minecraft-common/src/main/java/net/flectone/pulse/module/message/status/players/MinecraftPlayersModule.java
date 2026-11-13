package net.flectone.pulse.module.message.status.players;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.module.message.status.players.listener.MinecraftPulsePlayersListener;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.service.SocialService;

@Singleton
public class MinecraftPlayersModule extends PlayersModuleImpl {

    private final ListenerRegistry listenerRegistry;

    @Inject
    public MinecraftPlayersModule(FileFacade fileFacade,
                                  PermissionChecker permissionChecker,
                                  PlatformServerAdapter platformServerAdapter,
                                  ModuleController moduleController,
                                  MessagePipeline messagePipeline,
                                  SocialService socialService,
                                  ListenerRegistry listenerRegistry) {
        super(fileFacade, permissionChecker, platformServerAdapter, moduleController, messagePipeline, socialService);

        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        listenerRegistry.register(MinecraftPulsePlayersListener.class);
    }

}
