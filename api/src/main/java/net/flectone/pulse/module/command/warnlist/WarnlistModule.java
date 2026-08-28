package net.flectone.pulse.module.command.warnlist;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;

/**
 * The /warnlist command, which pages through the active warnings.
 * @author TheFaser
 */
public interface WarnlistModule extends ModuleCommand {

    @Override
    Command.Warnlist config();

    @Override
    Permission.Command.Warnlist permission();

    @Override
    Localization.Command.Warnlist localization(FPlayer fPlayer);

}
