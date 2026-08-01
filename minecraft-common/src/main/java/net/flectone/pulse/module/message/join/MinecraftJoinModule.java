package net.flectone.pulse.module.message.join;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.module.message.join.listener.MinecraftPulseJoinListener;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.service.PlaytimeService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
public class MinecraftJoinModule extends JoinModule {

    private final ListenerRegistry listenerRegistry;

    @Inject
    public MinecraftJoinModule(FileFacade fileFacade,
                               PlatformPlayerAdapter platformPlayerAdapter,
                               MessageDispatcher messageDispatcher,
                               ModuleController moduleController,
                               PlaytimeService playtimeService,
                               SocialService socialService,
                               ProxyRegistry proxyRegistry,
                               ListenerRegistry listenerRegistry) {
        super(fileFacade, platformPlayerAdapter, messageDispatcher, moduleController, playtimeService, socialService, proxyRegistry, listenerRegistry);

        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(MinecraftPulseJoinListener.class);
    }

}
