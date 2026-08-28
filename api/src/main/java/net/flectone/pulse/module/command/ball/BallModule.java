package net.flectone.pulse.module.command.ball;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;

/**
 * The /ball command, a magic-eight-ball that answers a yes-or-no question at random.
 * @author TheFaser
 */
public interface BallModule extends ModuleCommand {

    @Override
    Command.Ball config();

    @Override
    Permission.Command.Ball permission();

    @Override
    Localization.Command.Ball localization(FPlayer fPlayer);

    /**
     * The wording of one answer in the reader's language.
     *
     * @param fPlayer the reader
     * @param answer index of the answer
     * @return the answer text
     */
    String replaceAnswer(FPlayer fPlayer, int answer);

}
