package net.flectone.pulse.module.command.geolocate;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.util.constant.ModuleName;
import org.incendo.cloud.context.CommandContext;

/**
 * The /geolocate command, which looks up where a player is connecting from.
 * @author TheFaser
 */
public interface GeolocateModule extends ModuleCommand {

    @Override
    void onEnable();

    @Override
    void onDisable();

    @Override
    void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext);

    @Override
    ModuleName name();

    @Override
    Command.Geolocate config();

    @Override
    Permission.Command.Geolocate permission();

    @Override
    Localization.Command.Geolocate localization(FPlayer fPlayer);

}
