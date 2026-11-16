package net.flectone.pulse.module.message.quit;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.dispatcher.MessageDispatcher;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.module.message.quit.listener.MinecraftPulseQuitListener;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.service.PlayerSwitchService;
import net.flectone.pulse.service.SocialService;

@Singleton
public class MinecraftQuitModule extends QuitModuleImpl {

    private final ListenerRegistry listenerRegistry;

    @Inject
    public MinecraftQuitModule(FileFacade fileFacade,
                               MessageDispatcher messageDispatcher,
                               ModuleController moduleController,
                               PlatformServerAdapter platformServerAdapter,
                               ProxyRegistry proxyRegistry,
                               PlayerSwitchService playerSwitchService,
                               SocialService socialService,
                               ListenerRegistry listenerRegistry) {
        super(fileFacade, messageDispatcher, moduleController, platformServerAdapter, proxyRegistry, playerSwitchService, socialService, listenerRegistry);

        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(MinecraftPulseQuitListener.class);
    }

}
