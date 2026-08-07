package net.flectone.pulse.module.command.broadcast;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;
import org.incendo.cloud.context.CommandContext;

/**
 * The /broadcast command, which announces a message to everyone.
 * @author TheFaser
 */
public interface BroadcastModule extends ModuleCommand {

    @Override
    void onEnable();

    @Override
    void onDisable();

    @Override
    void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext);

    @Override
    ModuleName name();

    @Override
    Command.Broadcast config();

    @Override
    Permission.Command.Broadcast permission();

    @Override
    Localization.Command.Broadcast localization(FPlayer fPlayer);

}
