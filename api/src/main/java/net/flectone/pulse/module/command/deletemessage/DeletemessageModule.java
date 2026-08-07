package net.flectone.pulse.module.command.deletemessage;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;
import org.incendo.cloud.context.CommandContext;

/**
 * The /deletemessage command, which withdraws a message that was already delivered.
 * @author TheFaser
 */
public interface DeletemessageModule extends ModuleCommand {

    @Override
    void onEnable();

    @Override
    void onDisable();

    @Override
    void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext);

    @Override
    ModuleName name();

    @Override
    Command.Deletemessage config();

    @Override
    Permission.Command.Deletemessage permission();

    @Override
    Localization.Command.Deletemessage localization(FPlayer fPlayer);

}
