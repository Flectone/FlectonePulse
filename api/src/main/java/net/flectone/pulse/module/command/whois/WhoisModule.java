package net.flectone.pulse.module.command.whois;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;

/**
 * The /whois command, which shows what the plugin knows about a player.
 * @author TheFaser
 */
public interface WhoisModule extends ModuleCommand {

    @Override
    Command.Whois config();

    @Override
    Permission.Command.Whois permission();

    @Override
    Localization.Command.Whois localization(FPlayer fPlayer);

}
