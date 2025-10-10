package net.flectone.pulse.module.message.objective.tabname;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Ticker;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.objective.ObjectiveModule;
import net.flectone.pulse.module.message.objective.ScoreboardPosition;
import net.flectone.pulse.module.message.objective.tabname.listener.TabnamePulseListener;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TabnameModule extends AbstractModule {

    private final FileResolver fileResolver;
    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final TaskScheduler taskScheduler;
    private final ObjectiveModule objectiveModule;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        super.onEnable();

        Ticker ticker = config().getTicker();
        if (ticker.isEnable()) {
            taskScheduler.runAsyncTimer(() -> fPlayerService.getOnlineFPlayers().forEach(this::update), ticker.getPeriod());
        }

        listenerRegistry.register(TabnamePulseListener.class);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        fPlayerService.getPlatformFPlayers().forEach(this::remove);
    }

    @Override
    public Message.Objective.Tabname config() {
        return fileResolver.getMessage().getObjective().getTabname();
    }

    @Override
    public Permission.Message.Objective.Tabname permission() {
        return fileResolver.getPermission().getMessage().getObjective().getTabname();
    }

    public void create(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return;

        objectiveModule.createObjective(fPlayer, null, ScoreboardPosition.TABLIST);
        update(fPlayer);
    }

    public void update(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return;

        fPlayerService.getVisibleFPlayersFor(fPlayer).forEach(fObjective -> {
            int score = platformPlayerAdapter.getObjectiveScore(fObjective.getUuid(), config().getMode());
            objectiveModule.updateObjective(fPlayer, fObjective, score, ScoreboardPosition.TABLIST);
        });
    }

    public void remove(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return;

        objectiveModule.removeObjective(fPlayer, ScoreboardPosition.TABLIST);
    }
}
