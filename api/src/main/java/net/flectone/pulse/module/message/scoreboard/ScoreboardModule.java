package net.flectone.pulse.module.message.scoreboard;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleLocalization;
import org.jspecify.annotations.NonNull;

/**
 * Draws the scoreboard shown at the side of the screen.
 * @author TheFaser
 */
public interface ScoreboardModule extends ModuleLocalization {

    @Override
    ModuleName name();

    @Override
    Message.Scoreboard config();

    @Override
    Permission.Message.Scoreboard permission();

    @Override
    Localization.Message.Scoreboard localization(FPlayer fPlayer);

    /**
     * Shows the scoreboard to a player, or refreshes the one they already have.
     *
     * @param fPlayer the player
     */
    void createOrUpdate(@NonNull FPlayer fPlayer);

    /**
     * Takes the scoreboard away from a player.
     *
     * @param fPlayer the player
     */
    void remove(@NonNull FPlayer fPlayer);

}
