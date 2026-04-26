package net.flectone.pulse.module.message.bubble;

import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.module.message.bubble.listener.PulseBubbleListener;
import net.flectone.pulse.module.message.bubble.service.BubbleService;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public abstract class BubbleModule implements ModuleSimple {

    private final FileFacade fileFacade;
    private final TaskScheduler taskScheduler;
    private final BubbleService bubbleService;
    private final ListenerRegistry listenerRegistry;
    private final ModuleController moduleController;
    private final FLogger fLogger;

    private Pattern disallowedPattern;

    protected BubbleModule(FileFacade fileFacade,
                           TaskScheduler taskScheduler,
                           BubbleService bubbleService,
                           ListenerRegistry listenerRegistry,
                           ModuleController moduleController,
                           FLogger fLogger) {
        this.fileFacade = fileFacade;
        this.taskScheduler = taskScheduler;
        this.bubbleService = bubbleService;
        this.listenerRegistry = listenerRegistry;
        this.moduleController = moduleController;
        this.fLogger = fLogger;
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
        if (!config().disallowedInput().isEmpty()) {
            try {
                disallowedPattern = Pattern.compile(config().disallowedInput());
            } catch (PatternSyntaxException e) {
                fLogger.warning(e);
                return;
            }
        }

        bubbleService.startTicker();

        listenerRegistry.register(PulseBubbleListener.class);
    }

    @Override
    public void onDisable() {
        bubbleService.clear();
    }

    public void add(@NonNull FPlayer fPlayer, @NonNull String inputString, List<FPlayer> receivers) {
        add(fPlayer, inputString, inputString, receivers);
    }

    public void add(@NonNull FPlayer fPlayer, @NonNull String rawString, @NonNull String inputString, List<FPlayer> receivers) {
        if (disallowedPattern != null && disallowedPattern.matcher(rawString).matches()) return;

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
