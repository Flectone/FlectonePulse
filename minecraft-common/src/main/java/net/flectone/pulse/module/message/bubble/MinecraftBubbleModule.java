package net.flectone.pulse.module.message.bubble;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.converter.ColorConverter;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.logging.FLogger;
import net.flectone.pulse.module.message.bubble.listener.MinecraftPacketBubbleListener;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.platform.render.HeadTextRender;
import net.flectone.pulse.service.SocialService;

@Singleton
public class MinecraftBubbleModule extends BubbleModuleImpl {

    private final ListenerRegistry listenerRegistry;

    @Inject
    public MinecraftBubbleModule(FileFacade fileFacade,
                                 TaskScheduler taskScheduler,
                                 ModuleController moduleController,
                                 HeadTextRender headTextRender,
                                 SocialService socialService,
                                 MessagePipeline messagePipeline,
                                 FLogger fLogger,
                                 ListenerRegistry listenerRegistry) {
        super(fileFacade, taskScheduler, moduleController, headTextRender, socialService, messagePipeline, fLogger);

        this.listenerRegistry = listenerRegistry;
    }


    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(MinecraftPacketBubbleListener.class);
    }

}
