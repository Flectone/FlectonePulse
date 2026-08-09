package net.flectone.pulse.module.message.bubble;

import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.logging.FLogger;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.message.bubble.listener.PulseBubbleListener;
import net.flectone.pulse.module.message.bubble.service.BubbleService;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.scheduler.TaskScheduler;
import org.jspecify.annotations.NonNull;

import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public abstract class BubbleModuleImpl implements BubbleModule {

    private final FileFacade fileFacade;
    private final TaskScheduler taskScheduler;
    private final BubbleService bubbleService;
    private final ListenerRegistry listenerRegistry;
    private final ModuleController moduleController;
    private final FLogger fLogger;

    private Predicate<String> disallowedPredicate;

    protected BubbleModuleImpl(FileFacade fileFacade,
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
        if (config().disallowedInput().isEmpty()) {
            disallowedPredicate = null;
        } else {
            try {
                disallowedPredicate = Pattern.compile(config().disallowedInput()).asMatchPredicate();
            } catch (PatternSyntaxException e) {
                fLogger.warning(e);
            }
        }

        bubbleService.startTicker();

        listenerRegistry.register(PulseBubbleListener.class);
    }

    @Override
    public void onDisable() {
        bubbleService.clear();
    }

    @Override
    public void add(@NonNull FPlayer fPlayer, @NonNull String inputString, Set<FPlayer> receivers) {
        add(fPlayer, inputString, inputString, receivers);
    }

    @Override
    public void add(@NonNull FPlayer fPlayer, @NonNull String rawString, @NonNull String inputString, Set<FPlayer> receivers) {
        if (disallowedPredicate != null && disallowedPredicate.test(rawString)) return;

        taskScheduler.runAsync(() -> {
            if (moduleController.isDisabledFor(this, fPlayer)) return;

            bubbleService.addMessage(fPlayer, inputString, receivers);
        });
    }

}
