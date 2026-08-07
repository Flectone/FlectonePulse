package net.flectone.pulse.module.command.symbol;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;
import org.incendo.cloud.context.CommandContext;

/**
 * The /symbol command, which lists the symbols a player may insert into chat.
 * @author TheFaser
 */
public interface SymbolModule extends ModuleCommand {

    @Override
    void onEnable();

    @Override
    void onDisable();

    @Override
    void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext);

    @Override
    ModuleName name();

    @Override
    Command.Symbol config();

    @Override
    Permission.Command.Symbol permission();

    @Override
    Localization.Command.Symbol localization(FPlayer fPlayer);

}
