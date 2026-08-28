package net.flectone.pulse.module.command.ignore;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;

/**
 * The /ignore command, which hides another player's messages.
 * @author TheFaser
 */
public interface IgnoreModule extends ModuleCommand {

    @Override
    Command.Ignore config();

    @Override
    Permission.Command.Ignore permission();

    @Override
    Localization.Command.Ignore localization(FPlayer fPlayer);

}
