package net.flectone.pulse.module.message.afk;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.module.message.afk.listener.BukkitAfkListener;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.formatter.TimeFormatter;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.PlaytimeService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
public class BukkitAfkModule extends AfkModule {

    private final ListenerRegistry listenerRegistry;

    @Inject
    public BukkitAfkModule(FileFacade fileFacade,
                           FPlayerService fPlayerService,
                           TaskScheduler taskScheduler,
                           PlatformPlayerAdapter platformPlayerAdapter,
                           ListenerRegistry listenerRegistry,
                           MessagePipeline messagePipeline,
                           MessageDispatcher messageDispatcher,
                           ModuleController moduleController,
                           TimeFormatter timeFormatter,
                           PlaytimeService playtimeService,
                           SocialService socialService,
                           ProxyRegistry proxyRegistry) {
        super(fileFacade, fPlayerService, taskScheduler, platformPlayerAdapter, listenerRegistry, messagePipeline, messageDispatcher, moduleController, timeFormatter, playtimeService, socialService, proxyRegistry);

        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(BukkitAfkListener.class);
    }
}
