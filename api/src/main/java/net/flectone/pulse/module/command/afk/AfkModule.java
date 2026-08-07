package net.flectone.pulse.module.command.afk;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;
import org.incendo.cloud.context.CommandContext;

/**
 * The /afk command, which marks a player away by hand.
 * @author TheFaser
 */
public interface AfkModule extends ModuleCommand {

    @Override
    void onEnable();

    @Override
    ModuleName name();

    @Override
    Command.Afk config();

    @Override
    Permission.Command.Afk permission();

    @Override
    Localization.Command localization(FPlayer fPlayer);

    @Override
    void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext);

}
