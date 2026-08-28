package net.flectone.pulse.module.command.symbol;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;

/**
 * The /symbol command, which lists the symbols a player may insert into chat.
 * @author TheFaser
 */
public interface SymbolModule extends ModuleCommand {

    @Override
    Command.Symbol config();

    @Override
    Permission.Command.Symbol permission();

    @Override
    Localization.Command.Symbol localization(FPlayer fPlayer);

}
