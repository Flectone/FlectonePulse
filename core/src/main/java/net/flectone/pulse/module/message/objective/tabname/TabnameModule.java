package net.flectone.pulse.module.message.objective.tabname;

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
import net.flectone.pulse.module.message.objective.tabname.listener.TabnamePulseListener;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TabnameModule extends AbstractModuleLocalization<Localization.Message.Objective.Tabname> {

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

        Component tabName = createTabName(fPlayer, fPlayer);

        objectiveModule.createObjective(fPlayer, null, tabName, ScoreboardPosition.TABLIST);
        update(fPlayer);
    }

    public void update(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return;

        fPlayerService.getVisibleFPlayersFor(fPlayer).forEach(fObjective -> {
            int score = platformPlayerAdapter.getObjectiveScore(fObjective.getUuid(), config().mode());
            Component tabName = createTabName(fObjective, fPlayer);
            objectiveModule.updateObjective(fPlayer, fObjective, score, tabName, ScoreboardPosition.TABLIST);
        });
    }

    public void remove(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return;

        objectiveModule.removeObjective(fPlayer, ScoreboardPosition.TABLIST);
    }

    private Component createTabName(FPlayer fPlayer, FPlayer fReceiver) {
        int score = platformPlayerAdapter.getObjectiveScore(fPlayer.getUuid(), config().mode());

        MessageContext tabNameContext = messagePipeline.createContext(fPlayer, fReceiver, localization(fReceiver).format())
                .addTagResolver(Placeholder.parsed("score", String.valueOf(score)));
        return messagePipeline.build(tabNameContext);
    }
}
