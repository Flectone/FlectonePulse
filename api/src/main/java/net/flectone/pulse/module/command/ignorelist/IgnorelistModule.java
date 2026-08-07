package net.flectone.pulse.module.command.ignorelist;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.util.constant.ModuleName;
import org.incendo.cloud.context.CommandContext;

/**
 * The /ignorelist command, which shows who the player is ignoring.
 * @author TheFaser
 */
public interface IgnorelistModule extends ModuleCommand {

    @Override
    void onEnable();

    @Override
    void onDisable();

    @Override
    void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext);

    @Override
    ModuleName name();

    @Override
    Command.Ignorelist config();

    @Override
    Permission.Command.Ignorelist permission();

    @Override
    Localization.Command.Ignorelist localization(FPlayer fPlayer);

}
