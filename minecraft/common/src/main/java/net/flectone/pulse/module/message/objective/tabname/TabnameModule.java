package net.flectone.pulse.module.message.objective.tabname;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Ticker;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.objective.MinecraftObjectiveModule;
import net.flectone.pulse.module.message.objective.ScoreboardPosition;
import net.flectone.pulse.module.message.objective.tabname.listener.TabnamePulseListener;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.Component;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TabnameModule extends AbstractModuleLocalization<Localization.Message.Objective.Tabname> {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final TaskScheduler taskScheduler;
    private final MinecraftObjectiveModule objectiveModule;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        super.onEnable();

        Ticker ticker = config().ticker();
        if (ticker.enable()) {
            taskScheduler.runPlayerRegionTimer(this::updateScore, ticker.period());
        }

        listenerRegistry.register(TabnamePulseListener.class);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        fPlayerService.getPlatformFPlayers().forEach(this::remove);
    }

    @Override
    public MessageType messageType() {
        return MessageType.TABNAME;
    }

    @Override
    public Message.Objective.Tabname config() {
        return fileFacade.message().objective().tabname();
    }

    @Override
    public Permission.Message.Objective.Tabname permission() {
        return fileFacade.permission().message().objective().tabname();
    }

    @Override
    public Localization.Message.Objective.Tabname localization(FEntity sender) {
        return fileFacade.localization(sender).message().objective().tabname();
    }

    public void create(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return;

        Localization.Message.Objective.Tabname localization = localization(fPlayer);
        Component displayFormat = objectiveModule.buildFormat(fPlayer, fPlayer, localization.score(), localization.displayFormat());
        Component scoreFormat = objectiveModule.buildFormat(fPlayer, fPlayer, localization.score(), localization.scoreFormat());

        objectiveModule.createObjective(fPlayer, displayFormat, scoreFormat, ScoreboardPosition.TABLIST);
        updateScore(fPlayer);
    }

    public void updateScore(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return;

        fPlayerService.getVisibleFPlayersFor(fPlayer).forEach(fObjective -> {
            Localization.Message.Objective.Tabname localization = localization(fPlayer);
            Component scoreFormat = objectiveModule.buildFormat(fObjective, fPlayer, localization.score(), localization.scoreFormat());

            objectiveModule.updateObjective(fPlayer, fObjective, scoreFormat, ScoreboardPosition.TABLIST);
        });
    }

    public void remove(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return;

        objectiveModule.removeObjective(fPlayer, ScoreboardPosition.TABLIST);
    }
}
