package net.flectone.pulse.module.message.objective.belowname;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.ScoreboardPosition;
import net.flectone.pulse.model.Ticker;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.objective.ObjectiveModule;
import net.flectone.pulse.registry.EventProcessRegistry;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.service.FPlayerService;

@Singleton
public class BelownameModule extends AbstractModule {

    private final Message.Objective.Belowname config;
    private final Permission.Message.Objective.Belowname permission;
    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final TaskScheduler taskScheduler;
    private final ObjectiveModule objectiveModule;
    private final EventProcessRegistry eventProcessRegistry;

    @Inject
    public BelownameModule(FileResolver fileResolver,
                           FPlayerService fPlayerService,
                           PlatformPlayerAdapter platformPlayerAdapter,
                           TaskScheduler taskScheduler,
                           ObjectiveModule objectiveModule,
                           EventProcessRegistry eventProcessRegistry) {
        this.config = fileResolver.getMessage().getObjective().getBelowname();
        this.permission = fileResolver.getPermission().getMessage().getObjective().getBelowname();
        this.fPlayerService = fPlayerService;
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.taskScheduler = taskScheduler;
        this.objectiveModule = objectiveModule;
        this.eventProcessRegistry = eventProcessRegistry;
    }

    @Override
    public void onEnable() {
        fPlayerService.getFPlayers().forEach(this::create);

        registerModulePermission(permission);

        Ticker ticker = config.getTicker();
        if (ticker.isEnable()) {
            taskScheduler.runAsyncTimer(() -> fPlayerService.getFPlayers().forEach(this::update), ticker.getPeriod());
        }

        eventProcessRegistry.registerPlayerHandler(Event.Type.PLAYER_LOAD, this::create);
        eventProcessRegistry.registerPlayerHandler(Event.Type.PLAYER_QUIT, this::remove);
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
        if (checkModulePredicates(fPlayer)) return;

        objectiveModule.createObjective(fPlayer, ScoreboardPosition.BELOWNAME);
        update(fPlayer);
    }

    public void update(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return;

        int score = platformPlayerAdapter.getObjectiveScore(fPlayer.getUuid(), config.getMode());
        objectiveModule.updateObjective(fPlayer, score, ScoreboardPosition.BELOWNAME);
    }

    public void remove(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return;

        objectiveModule.removeObjective(fPlayer, ScoreboardPosition.BELOWNAME);
    }
}
