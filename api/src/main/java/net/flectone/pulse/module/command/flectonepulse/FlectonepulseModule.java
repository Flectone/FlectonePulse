package net.flectone.pulse.module.command.flectonepulse;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.util.constant.ModuleName;
import org.incendo.cloud.context.CommandContext;

/**
 * The /flectonepulse command, which reloads the plugin and reports its status.
 * @author TheFaser
 */
public interface FlectonepulseModule extends ModuleCommand {

    @Override
    void onEnable();

    @Override
    void onDisable();

    @Override
    void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext);

    /**
     * Reloads the plugin and reports the outcome.
     *
     * @param fPlayer who asked for the reload
     */
    void reload(FPlayer fPlayer);

    @Override
    ModuleName name();

    @Override
    Command.Flectonepulse config();

    @Override
    Permission.Command.Flectonepulse permission();

    @Override
    Localization.Command.Flectonepulse localization(FPlayer fPlayer);

}
