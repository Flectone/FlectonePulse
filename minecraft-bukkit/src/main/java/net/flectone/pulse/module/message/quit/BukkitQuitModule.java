package net.flectone.pulse.module.message.quit;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.module.message.quit.listener.BukkitQuitListener;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
public class BukkitQuitModule extends MinecraftQuitModule {

    private final ListenerRegistry listenerRegistry;

    @Inject
    public BukkitQuitModule(FileFacade fileFacade,
                            TaskScheduler taskScheduler,
                            MessageDispatcher messageDispatcher,
                            ModuleController moduleController,
                            PlatformServerAdapter platformServerAdapter,
                            ProxyRegistry proxyRegistry,
                            FPlayerService fPlayerService,
                            SocialService socialService,
                            ListenerRegistry listenerRegistry) {
        super(fileFacade, taskScheduler, messageDispatcher, moduleController, platformServerAdapter, proxyRegistry, fPlayerService, socialService, listenerRegistry);

        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(BukkitQuitListener.class);
    }
}
