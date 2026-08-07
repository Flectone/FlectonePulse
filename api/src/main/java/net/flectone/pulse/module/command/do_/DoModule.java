package net.flectone.pulse.module.command.do_;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;
import org.incendo.cloud.context.CommandContext;

/**
 * The /do command, which narrates an action in the third person.
 * @author TheFaser
 */
public interface DoModule extends ModuleCommand {

    @Override
    void onEnable();

    @Override
    void onDisable();

    @Override
    void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext);

    @Override
    ModuleName name();

    @Override
    Command.CommandDo config();

    @Override
    Permission.Command.CommandDo permission();

    @Override
    Localization.Command.CommandDo localization(FPlayer fPlayer);

}
