package net.flectone.pulse.module.command.mutelist;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.util.constant.ModuleName;
import org.incendo.cloud.context.CommandContext;

/**
 * The /mutelist command, which pages through the active mutes.
 * @author TheFaser
 */
public interface MutelistModule extends ModuleCommand {

    @Override
    void onEnable();

    @Override
    void onDisable();

    @Override
    void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext);

    @Override
    ModuleName name();

    @Override
    Command.Mutelist config();

    @Override
    Permission.Command.Mutelist permission();

    @Override
    Localization.Command.Mutelist localization(FPlayer fPlayer);

}
