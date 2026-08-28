package net.flectone.pulse.module.command.banlist;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;

/**
 * The /banlist command, which pages through the active bans.
 * @author TheFaser
 */
public interface BanlistModule extends ModuleCommand {

    @Override
    Command.Banlist config();

    @Override
    Permission.Command.Banlist permission();

    @Override
    Localization.Command.Banlist localization(FPlayer fPlayer);

}
