package net.flectone.pulse.module.command.helper;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.util.constant.ModuleName;
import org.incendo.cloud.context.CommandContext;

import java.util.function.Predicate;

/**
 * The /helper command, which asks the staff on duty for help.
 * @author TheFaser
 */
public interface HelperModule extends ModuleCommand {

    @Override
    void onEnable();

    @Override
    void onDisable();

    @Override
    void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext);

    @Override
    ModuleName name();

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
