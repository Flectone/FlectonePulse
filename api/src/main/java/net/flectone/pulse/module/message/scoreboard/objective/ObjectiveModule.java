package net.flectone.pulse.module.message.scoreboard.objective;

import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleSimple;
import net.kyori.adventure.text.Component;

/**
 * Groups the scoreboard objectives shown below a player's name and in the tab list.
 * @author TheFaser
 */
public interface ObjectiveModule extends ModuleSimple {

    @Override
    Message.Scoreboard.Objective config();

    @Override
    Permission.Message.Scoreboard.Objective permission();

    /**
     * Creates an objective for a player, or replaces the one they already have.
     *
     * @param fPlayer the player whose objective is created
     * @param displayName the objective title
     * @param scoreFormat the format of every score in the objective
     * @param scoreboardPosition where the objective is displayed
     */
    void createObjective(FPlayer fPlayer, Component displayName, Component scoreFormat, ScoreboardPosition scoreboardPosition);

    /**
     * Updates the score of one player inside another player's objective.
     *
     * @param fPlayer the player whose objective is updated
     * @param fObjective the player the score belongs to
     * @param scoreFormat the new score format
     * @param scoreboardPosition where the objective is displayed
     */
    void updateObjective(FPlayer fPlayer, FPlayer fObjective, Component scoreFormat, ScoreboardPosition scoreboardPosition);

    /**
     * Removes an objective from a player.
     *
     * @param fPlayer the player whose objective is removed
     * @param scoreboardPosition where the objective is displayed
     */
    void removeObjective(FPlayer fPlayer, ScoreboardPosition scoreboardPosition);

    /**
     * Forgets the score remembered for a player, so the next update is sent again.
     *
     * @param fPlayer the player whose objective is updated
     * @param fObjective the player the score belongs to
     * @param scoreboardPosition where the objective is displayed
     */
    void forgetScore(FPlayer fPlayer, FPlayer fObjective, ScoreboardPosition scoreboardPosition);

    /**
     * Builds the text of an objective.
     *
     * @param fPlayer the player whose objective is built
     * @param fReceiver the player who will see it
     * @param score the score text
     * @param format the format containing the score tag
     * @return the built component
     */
    Component buildFormat(FPlayer fPlayer, FPlayer fReceiver, String score, String format);

    /**
     * Builds the text of an objective.
     *
     * @param fPlayer the player whose objective is built
     * @param fReceiver the player who will see it
     * @param score the score text
     * @param format the format containing the score tag
     * @param colorContextSender whether the sender's color context is used
     * @return the built component
     */
    Component buildFormat(FPlayer fPlayer, FPlayer fReceiver, String score, String format, boolean colorContextSender);

}
