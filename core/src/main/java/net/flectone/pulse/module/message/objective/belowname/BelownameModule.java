package net.flectone.pulse.module.message.objective.belowname;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Ticker;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.objective.ObjectiveModule;
import net.flectone.pulse.module.message.objective.ScoreboardPosition;
import net.flectone.pulse.module.message.objective.belowname.listener.BelownamePulseListener;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageType;
import net.kyori.adventure.text.Component;

@Singleton
public class BelownameModule extends AbstractModuleLocalization<Localization.Message.Objective.Belowname> {

    private final FileResolver fileResolver;
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
        super(MessageType.OBJECTIVE);

        this.fileResolver = fileResolver;
        this.fPlayerService = fPlayerService;
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.taskScheduler = taskScheduler;
        this.objectiveModule = objectiveModule;
        this.listenerRegistry = listenerRegistry;
        this.messagePipeline = messagePipeline;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission());

        Ticker ticker = config().getTicker();
        if (ticker.isEnable()) {
            taskScheduler.runAsyncTimer(() -> fPlayerService.getOnlineFPlayers().forEach(this::update), ticker.getPeriod());
        }

        listenerRegistry.register(BelownamePulseListener.class);
    }

    @Override
    public void onDisable() {
        fPlayerService.getPlatformFPlayers().forEach(this::remove);
    }

    @Override
    public Message.Objective.Belowname config() {
        return fileResolver.getMessage().getObjective().getBelowname();
    }

    @Override
    public Permission.Message.Objective.Belowname permission() {
        return fileResolver.getPermission().getMessage().getObjective().getBelowname();
    }

    @Override
    public Localization.Message.Objective.Belowname localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getObjective().getBelowname();
    }

    public void create(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return;

        Component displayName = messagePipeline.builder(fPlayer, localization(fPlayer).getFormat())
                .build();

        objectiveModule.createObjective(fPlayer, displayName, ScoreboardPosition.BELOWNAME);
        update(fPlayer);
    }

    public void update(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return;

        fPlayerService.getVisibleFPlayersFor(fPlayer).forEach(fObjective -> {
            int score = platformPlayerAdapter.getObjectiveScore(fObjective.getUuid(), config().getMode());
            objectiveModule.updateObjective(fPlayer, fObjective, score, ScoreboardPosition.BELOWNAME);
        });
    }

    public void remove(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return;

        objectiveModule.removeObjective(fPlayer, ScoreboardPosition.BELOWNAME);
    }
}
