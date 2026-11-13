package net.flectone.pulse.module.message.scoreboard.objective.belowname;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleLocalization;

/**
 * Draws the score shown below a player's name.
 * @author TheFaser
 */
public interface BelownameModule extends ModuleLocalization {

    @Override
    Message.Scoreboard.Objective.Belowname config();

    @Override
    Permission.Message.Scoreboard.Objective.Belowname permission();

    @Override
    Localization.Message.Scoreboard.Objective.Belowname localization(FPlayer fPlayer);

    /**
     * Shows the score below a player's name, or refreshes it.
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
