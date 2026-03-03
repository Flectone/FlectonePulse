package net.flectone.pulse.module.message.bubble;

import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.module.message.bubble.listener.BubblePulseListener;
import net.flectone.pulse.module.message.bubble.service.BubbleService;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import org.jspecify.annotations.NonNull;

import java.util.List;

public abstract class BubbleModule implements ModuleSimple {

    private final FileFacade fileFacade;
    private final TaskScheduler taskScheduler;
    private final BubbleService bubbleService;
    private final ListenerRegistry listenerRegistry;
    private final ModuleController moduleController;

    protected BubbleModule(FileFacade fileFacade,
                           TaskScheduler taskScheduler,
                           BubbleService bubbleService,
                           ListenerRegistry listenerRegistry,
                           ModuleController moduleController) {
        this.fileFacade = fileFacade;
        this.taskScheduler = taskScheduler;
        this.bubbleService = bubbleService;
        this.listenerRegistry = listenerRegistry;
        this.moduleController = moduleController;
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_BUBBLE;
    }

    @Override
    public Message.Bubble config() {
        return fileFacade.message().bubble();
    }

    @Override
    public Permission.Message.Bubble permission() {
        return fileFacade.permission().message().bubble();
    }

    @Override
    public void onEnable() {
        bubbleService.startTicker();

        listenerRegistry.register(BubblePulseListener.class);
    }

    @Override
    public void onDisable() {
        bubbleService.clear();
    }

    public void add(@NonNull FPlayer fPlayer, @NonNull String inputString, List<FPlayer> receivers) {
        taskScheduler.runRegion(fPlayer, () -> {
            if (moduleController.isDisabledFor(this, fPlayer)) return;

            bubbleService.addMessage(fPlayer, inputString, receivers);
        });
    }

    public enum Billboard {

        FIXED,
        VERTICAL,
        HORIZONTAL,
        CENTER

    }
}
