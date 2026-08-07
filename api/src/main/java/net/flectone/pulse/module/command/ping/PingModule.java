package net.flectone.pulse.module.command.ping;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.util.constant.ModuleName;
import org.incendo.cloud.context.CommandContext;

/**
 * The /ping command, which reports a player's latency.
 * @author TheFaser
 */
public interface PingModule extends ModuleCommand {

    @Override
    void onEnable();

    @Override
    void onDisable();

    @Override
    void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext);

    @Override
    ModuleName name();

    @Override
    Command.Ping config();

    @Override
    Permission.Command.Ping permission();

    @Override
    Localization.Command.Ping localization(FPlayer fPlayer);

}
