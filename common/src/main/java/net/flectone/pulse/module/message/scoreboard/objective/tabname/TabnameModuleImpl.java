package net.flectone.pulse.module.message.scoreboard.objective.tabname;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.constant.SettingText;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.value.Ticker;
import net.flectone.pulse.module.message.scoreboard.objective.ObjectiveModule;
import net.flectone.pulse.module.message.scoreboard.objective.ScoreboardPosition;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;
import net.kyori.adventure.text.Component;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TabnameModuleImpl implements TabnameModule {

    private final FileFacade fileFacade;
    private final SocialService socialService;
    private final FPlayerService fPlayerService;
    private final TaskScheduler taskScheduler;
    private final ModuleController moduleController;
    private final ObjectiveModule objectiveModule;

    @Override
    public void onEnable() {
        Ticker ticker = config().ticker();
        if (ticker.enable()) {
            taskScheduler.runPlayerAsyncTimer(this::updateScore, ticker.period());
        }
    }

    @Override
    public void onDisable() {
        fPlayerService.getPlatformFPlayers().forEach(this::remove);
    }

    @Override
    public void create(FPlayer fPlayer) {
        if (moduleController.isDisabledFor(this, fPlayer)) return;

        Localization.Message.Scoreboard.Objective.Tabname localization = localization(fPlayer);
        Component displayFormat = objectiveModule.buildFormat(fPlayer, fPlayer, localization.score(), localization.displayFormat());
        Component scoreFormat = objectiveModule.buildFormat(fPlayer, fPlayer, localization.score(), localization.scoreFormat());

        objectiveModule.createObjective(fPlayer, displayFormat, scoreFormat, ScoreboardPosition.TABLIST);
        updateScore(fPlayer);
    }

    @Override
    public void updateScore(FPlayer fPlayer) {
        if (moduleController.isDisabledFor(this, fPlayer)) return;

        fPlayerService.getOnlineFPlayers().forEach(fObjective -> {
            if (!socialService.canSeeVanished(fObjective, fPlayer)) {
                objectiveModule.forgetScore(fPlayer, fObjective, ScoreboardPosition.TABLIST);
                return;
            }

            Localization.Message.Scoreboard.Objective.Tabname localization = localization(fPlayer);
            Component scoreFormat = objectiveModule.buildFormat(fObjective, fPlayer, localization.score(), localization.scoreFormat());

            objectiveModule.updateObjective(fPlayer, fObjective, scoreFormat, ScoreboardPosition.TABLIST);
        });
    }

    @Override
    public void remove(FPlayer fPlayer) {
        objectiveModule.removeObjective(fPlayer, ScoreboardPosition.TABLIST);
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

}
