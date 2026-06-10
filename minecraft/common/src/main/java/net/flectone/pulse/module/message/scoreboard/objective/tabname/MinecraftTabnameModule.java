package net.flectone.pulse.module.message.scoreboard.objective.tabname;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Ticker;
import net.flectone.pulse.module.ModuleLocalization;
import net.flectone.pulse.module.message.scoreboard.objective.MinecraftObjectiveModule;
import net.flectone.pulse.module.message.scoreboard.objective.ScoreboardPosition;
import net.flectone.pulse.module.message.scoreboard.objective.tabname.listener.MinecraftPulseTabnameListener;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.Component;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftTabnameModule implements ModuleLocalization<Localization.Message.Scoreboard.Objective.Tabname> {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final TaskScheduler taskScheduler;
    private final MinecraftObjectiveModule objectiveModule;
    private final ListenerRegistry listenerRegistry;
    private final ModuleController moduleController;
    private final SocialService socialService;

    @Override
    public void onEnable() {
        Ticker ticker = config().ticker();
        if (ticker.enable()) {
            taskScheduler.runPlayerAsyncTimer(this::updateScore, ticker.period());
        }

        listenerRegistry.register(MinecraftPulseTabnameListener.class);
    }

    @Override
    public void onDisable() {
        fPlayerService.getPlatformFPlayers().forEach(this::remove);
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_SCOREBOARD_OBJECTIVE_TABNAME;
    }

    @Override
    public Message.Scoreboard.Objective.Tabname config() {
        return fileFacade.message().scoreboard().objective().tabname();
    }

    @Override
    public Permission.Message.Scoreboard.Objective.Tabname permission() {
        return fileFacade.permission().message().scoreboard().objective().tabname();
    }

    @Override
    public Localization.Message.Scoreboard.Objective.Tabname localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).message().scoreboard().objective().tabname();
    }

    public void create(FPlayer fPlayer) {
        if (moduleController.isDisabledFor(this, fPlayer)) return;

        Localization.Message.Scoreboard.Objective.Tabname localization = localization(fPlayer);
        Component displayFormat = objectiveModule.buildFormat(fPlayer, fPlayer, localization.score(), localization.displayFormat());
        Component scoreFormat = objectiveModule.buildFormat(fPlayer, fPlayer, localization.score(), localization.scoreFormat());

        objectiveModule.createObjective(fPlayer, displayFormat, scoreFormat, ScoreboardPosition.TABLIST);
        updateScore(fPlayer);
    }

    public void updateScore(FPlayer fPlayer) {
        if (moduleController.isDisabledFor(this, fPlayer)) return;

        fPlayerService.getOnlineFPlayers().stream()
                .filter(vanishedPlayer -> socialService.canSeeVanished(vanishedPlayer, fPlayer))
                .forEach(fObjective -> {
                    Localization.Message.Scoreboard.Objective.Tabname localization = localization(fPlayer);
                    Component scoreFormat = objectiveModule.buildFormat(fObjective, fPlayer, localization.score(), localization.scoreFormat());

                    objectiveModule.updateObjective(fPlayer, fObjective, scoreFormat, ScoreboardPosition.TABLIST);
                });
    }

    public void remove(FPlayer fPlayer) {
        if (moduleController.isDisabledFor(this, fPlayer)) return;

        objectiveModule.removeObjective(fPlayer, ScoreboardPosition.TABLIST);
    }
}
