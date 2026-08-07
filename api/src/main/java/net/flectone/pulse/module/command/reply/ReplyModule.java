package net.flectone.pulse.module.command.reply;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;
import org.incendo.cloud.context.CommandContext;

/**
 * The /reply command, which answers the last private message received.
 * @author TheFaser
 */
public interface ReplyModule extends ModuleCommand {

    @Override
    void onEnable();

    @Override
    void onDisable();

    @Override
    void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext);

    @Override
    ModuleName name();

    @Override
    Command.Reply config();

    @Override
    Permission.Command.Reply permission();

    @Override
    Localization.Command.Reply localization(FPlayer fPlayer);

}
