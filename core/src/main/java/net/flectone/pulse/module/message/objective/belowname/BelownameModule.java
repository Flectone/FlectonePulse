package net.flectone.pulse.module.message.objective.belowname;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
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
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.Component;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BelownameModule extends AbstractModuleLocalization<Localization.Message.Objective.Belowname> {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final TaskScheduler taskScheduler;
    private final ObjectiveModule objectiveModule;
    private final ListenerRegistry listenerRegistry;
    private final MessagePipeline messagePipeline;

    @Override
    public void onEnable() {
        super.onEnable();

        Ticker ticker = config().ticker();
        if (ticker.enable()) {
            taskScheduler.runPlayerRegionTimer(this::update, ticker.period());
        }

        listenerRegistry.register(BelownamePulseListener.class);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        fPlayerService.getPlatformFPlayers().forEach(this::remove);
    }

    @Override
    public MessageType messageType() {
        return MessageType.OBJECTIVE;
    }

    @Override
    public Message.Objective.Belowname config() {
        return fileFacade.message().objective().belowname();
    }

    @Override
    public Permission.Message.Objective.Belowname permission() {
        return fileFacade.permission().message().objective().belowname();
    }

    @Override
    public Localization.Message.Objective.Belowname localization(FEntity sender) {
        return fileFacade.localization(sender).message().objective().belowname();
    }

    public void create(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return;

        Localization.Message.Objective.Belowname localization = localization(fPlayer);
        Component display = objectiveModule.buildFormat(fPlayer, fPlayer, localization.display(), config().mode());
        Component scoreFormat = objectiveModule.buildFormat(fPlayer, fPlayer, localization.format(), config().mode());

        objectiveModule.createObjective(fPlayer, display, scoreFormat, ScoreboardPosition.BELOWNAME);
        update(fPlayer);
    }

    public void update(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return;

        fPlayerService.getVisibleFPlayersFor(fPlayer).forEach(fObjective -> {
            int score = platformPlayerAdapter.getObjectiveScore(fObjective.getUuid(), config().mode());

            Component scoreFormat = objectiveModule.buildFormat(fObjective, fPlayer, localization(fPlayer).format(), config().mode());

            objectiveModule.updateObjective(fPlayer, fObjective, score, scoreFormat, ScoreboardPosition.BELOWNAME);
        });
    }

    public void remove(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return;

        objectiveModule.removeObjective(fPlayer, ScoreboardPosition.BELOWNAME);
    }


}
