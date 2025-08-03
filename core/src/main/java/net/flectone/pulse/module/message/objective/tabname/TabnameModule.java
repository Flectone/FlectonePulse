package net.flectone.pulse.module.message.objective.tabname;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Ticker;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.objective.ObjectiveModule;
import net.flectone.pulse.module.message.objective.ScoreboardPosition;
import net.flectone.pulse.module.message.objective.tabname.listener.TabnamePulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.service.FPlayerService;

@Singleton
public class TabnameModule extends AbstractModule {

    private final Message.Objective.Tabname config;
    private final Permission.Message.Objective.Tabname permission;
    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final TaskScheduler taskScheduler;
    private final ObjectiveModule objectiveModule;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public TabnameModule(FileResolver fileResolver,
                         FPlayerService fPlayerService,
                         PlatformPlayerAdapter platformPlayerAdapter,
                         TaskScheduler taskScheduler,
                         ObjectiveModule objectiveModule,
                         ListenerRegistry listenerRegistry) {
        this.config = fileResolver.getMessage().getObjective().getTabname();
        this.permission = fileResolver.getPermission().getMessage().getObjective().getTabname();
        this.fPlayerService = fPlayerService;
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.taskScheduler = taskScheduler;
        this.objectiveModule = objectiveModule;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        fPlayerService.getFPlayers().forEach(this::create);

        registerModulePermission(permission);

        Ticker ticker = config.getTicker();
        if (ticker.isEnable()) {
            taskScheduler.runAsyncTimer(() -> fPlayerService.getFPlayers().forEach(this::update), ticker.getPeriod());
        }

        listenerRegistry.register(TabnamePulseListener.class);
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

        objectiveModule.createObjective(fPlayer, null, ScoreboardPosition.TABLIST);
        update(fPlayer);
    }

    public void update(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return;

        fPlayerService.getPlatformFPlayers().forEach(fObjective -> {
            int score = platformPlayerAdapter.getObjectiveScore(fObjective.getUuid(), config.getMode());
            objectiveModule.updateObjective(fPlayer, fObjective, score, ScoreboardPosition.TABLIST);
        });
    }

    public void remove(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return;

        objectiveModule.removeObjective(fPlayer, ScoreboardPosition.TABLIST);
    }
}
