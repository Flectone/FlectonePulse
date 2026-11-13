package net.flectone.pulse.module.message.scoreboard.objective.tabname;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.module.message.scoreboard.objective.ObjectiveModule;
import net.flectone.pulse.module.message.scoreboard.objective.tabname.listener.MinecraftPulseTabnameListener;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;

@Singleton
public class MinecraftTabnameModule extends TabnameModuleImpl {

    private final ListenerRegistry listenerRegistry;

    @Inject
    public MinecraftTabnameModule(FileFacade fileFacade,
                                  SocialService socialService,
                                  FPlayerService fPlayerService,
                                  TaskScheduler taskScheduler,
                                  ModuleController moduleController,
                                  ObjectiveModule objectiveModule,
                                  ListenerRegistry listenerRegistry) {
        super(fileFacade, socialService, fPlayerService, taskScheduler, moduleController, objectiveModule);

        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(MinecraftPulseTabnameListener.class);
    }

}
