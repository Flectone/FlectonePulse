package net.flectone.pulse.module.command.clearmail;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;

/**
 * The /clearmail command, which deletes a mail the player sent.
 * @author TheFaser
 */
public interface ClearmailModule extends ModuleCommand {

    @Override
    Command.Clearmail config();

    @Override
    Permission.Command.Clearmail permission();

    @Override
    Localization.Command.Clearmail localization(FPlayer fPlayer);

}
