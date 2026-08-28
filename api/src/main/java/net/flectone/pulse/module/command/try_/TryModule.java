package net.flectone.pulse.module.command.try_;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;

/**
 * The /try command, which narrates an action that succeeds or fails at random.
 * @author TheFaser
 */
public interface TryModule extends ModuleCommand {

    @Override
    Command.CommandTry config();

    @Override
    Permission.Command.CommandTry permission();

    @Override
    Localization.Command.CommandTry localization(FPlayer fPlayer);

    /**
     * The wording of a roll result in the reader's language.
     *
     * @param fPlayer the reader
     * @param value the rolled percentage
     * @return the result text
     */
    String replacePercent(FPlayer fPlayer, int value);

}
