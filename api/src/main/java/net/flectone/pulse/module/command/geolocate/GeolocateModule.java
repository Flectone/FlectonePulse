package net.flectone.pulse.module.command.geolocate;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;

/**
 * The /geolocate command, which looks up where a player is connecting from.
 * @author TheFaser
 */
public interface GeolocateModule extends ModuleCommand {

    @Override
    Command.Geolocate config();

    @Override
    Permission.Command.Geolocate permission();

    @Override
    Localization.Command.Geolocate localization(FPlayer fPlayer);

}
