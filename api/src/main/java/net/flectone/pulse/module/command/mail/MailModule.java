package net.flectone.pulse.module.command.mail;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;
import org.incendo.cloud.context.CommandContext;

/**
 * The /mail command, which leaves a message for a player who is offline.
 * @author TheFaser
 */
public interface MailModule extends ModuleCommand {

    @Override
    void onEnable();

    @Override
    void onDisable();

    @Override
    void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext);

    @Override
    ModuleName name();

    @Override
    Command.Mail config();

    @Override
    Permission.Command.Mail permission();

    @Override
    Localization.Command.Mail localization(FPlayer fPlayer);

}
