package net.flectone.pulse.module.command.mail;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;

/**
 * The /mail command, which leaves a message for a player who is offline.
 * @author TheFaser
 */
public interface MailModule extends ModuleCommand {

    @Override
    Command.Mail config();

    @Override
    Permission.Command.Mail permission();

    @Override
    Localization.Command.Mail localization(FPlayer fPlayer);

}
