package net.flectone.pulse.module.command.helper;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;

import java.util.function.Predicate;

/**
 * The /helper command, which asks the staff on duty for help.
 * @author TheFaser
 */
public interface HelperModule extends ModuleCommand {

    @Override
    Command.Helper config();

    @Override
    Permission.Command.Helper permission();

    @Override
    Localization.Command.Helper localization(FPlayer fPlayer);

    /**
     * Which players count as staff and therefore see help requests.
     *
     * @return the staff filter
     */
    Predicate<FPlayer> getFilterSee();

}
