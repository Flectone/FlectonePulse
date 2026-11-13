package net.flectone.pulse.module.message.scoreboard.objective.tabname;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleLocalization;

/**
 * Draws the score shown next to a player in the tab list.
 * @author TheFaser
 */
public interface TabnameModule extends ModuleLocalization {

    @Override
    Message.Scoreboard.Objective.Tabname config();

    @Override
    Permission.Message.Scoreboard.Objective.Tabname permission();

    @Override
    Localization.Message.Scoreboard.Objective.Tabname localization(FPlayer fPlayer);

    /**
     * Shows the score next to a player in the tab list, or refreshes it.
     *
     * @param fPlayer the player
     */
    void create(FPlayer fPlayer);

    /**
     * Recalculates the scores of everyone visible to a player.
     *
     * @param fPlayer the player
     */
    void updateScore(FPlayer fPlayer);

    /**
     * Takes the score away from a player.
     *
     * @param fPlayer the player
     */
    void remove(FPlayer fPlayer);

}
