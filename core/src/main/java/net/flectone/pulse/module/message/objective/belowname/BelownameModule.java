package net.flectone.pulse.module.message.objective.belowname;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Ticker;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.objective.ObjectiveModule;
import net.flectone.pulse.module.message.objective.ScoreboardPosition;
import net.flectone.pulse.module.message.objective.belowname.listener.BelownamePulseListener;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.service.FPlayerService;
import net.kyori.adventure.text.Component;

@Singleton
public class BelownameModule extends AbstractModuleLocalization<Localization.Message.Objective.Belowname> {

    private final Message.Objective.Belowname config;
    private final Permission.Message.Objective.Belowname permission;
    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final TaskScheduler taskScheduler;
    private final ObjectiveModule objectiveModule;
    private final ListenerRegistry listenerRegistry;
    private final MessagePipeline messagePipeline;

    @Inject
    public BelownameModule(FileResolver fileResolver,
                           FPlayerService fPlayerService,
                           PlatformPlayerAdapter platformPlayerAdapter,
                           TaskScheduler taskScheduler,
                           ObjectiveModule objectiveModule,
                           ListenerRegistry listenerRegistry,
                           MessagePipeline messagePipeline) {
        super(localization -> localization.getMessage().getObjective().getBelowname());

        this.config = fileResolver.getMessage().getObjective().getBelowname();
        this.permission = fileResolver.getPermission().getMessage().getObjective().getBelowname();
        this.fPlayerService = fPlayerService;
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.taskScheduler = taskScheduler;
        this.objectiveModule = objectiveModule;
        this.listenerRegistry = listenerRegistry;
        this.messagePipeline = messagePipeline;
    }

    @Override
    public void onEnable() {
        fPlayerService.getFPlayers().forEach(this::create);

        registerModulePermission(permission);

        Ticker ticker = config.getTicker();
        if (ticker.isEnable()) {
            taskScheduler.runAsyncTimer(() -> fPlayerService.getFPlayers().forEach(this::update), ticker.getPeriod());
        }

        listenerRegistry.register(BelownamePulseListener.class);
    }

    @Override
    public void onDisable() {
        fPlayerService.getPlatformFPlayers().forEach(this::remove);
    }

    @Override
    protected boolean isConfigEnable() {
        return config.isEnable();
    }

    public void create(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return;

        Component displayName = messagePipeline.builder(fPlayer, resolveLocalization(fPlayer).getFormat())
                .build();

        objectiveModule.createObjective(fPlayer, displayName, ScoreboardPosition.BELOWNAME);
        update(fPlayer);
    }

    public void update(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return;

        fPlayerService.getPlatformFPlayers().forEach(fObjective -> {
            int score = platformPlayerAdapter.getObjectiveScore(fObjective.getUuid(), config.getMode());
            objectiveModule.updateObjective(fPlayer, fObjective, score, ScoreboardPosition.BELOWNAME);
        });
    }

    public void remove(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return;

        objectiveModule.removeObjective(fPlayer, ScoreboardPosition.BELOWNAME);
    }
}
