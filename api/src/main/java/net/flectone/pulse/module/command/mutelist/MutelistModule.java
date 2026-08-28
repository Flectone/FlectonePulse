package net.flectone.pulse.module.command.mutelist;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;

/**
 * The /mutelist command, which pages through the active mutes.
 * @author TheFaser
 */
public interface MutelistModule extends ModuleCommand {

    @Override
    Command.Mutelist config();

    @Override
    Permission.Command.Mutelist permission();

    @Override
    Localization.Command.Mutelist localization(FPlayer fPlayer);

}
