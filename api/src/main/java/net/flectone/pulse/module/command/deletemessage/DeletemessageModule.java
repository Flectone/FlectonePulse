package net.flectone.pulse.module.command.deletemessage;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;

/**
 * The /deletemessage command, which withdraws a message that was already delivered.
 * @author TheFaser
 */
public interface DeletemessageModule extends ModuleCommand {

    @Override
    Command.Deletemessage config();

    @Override
    Permission.Command.Deletemessage permission();

    @Override
    Localization.Command.Deletemessage localization(FPlayer fPlayer);

}
