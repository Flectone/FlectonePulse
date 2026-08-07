package net.flectone.pulse.module.command.banlist;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.util.constant.ModuleName;
import org.incendo.cloud.context.CommandContext;

/**
 * The /banlist command, which pages through the active bans.
 * @author TheFaser
 */
public interface BanlistModule extends ModuleCommand {

    @Override
    void onEnable();

    @Override
    void onDisable();

    @Override
    void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext);

    @Override
    ModuleName name();

    @Override
    Command.Banlist config();

    @Override
    Permission.Command.Banlist permission();

    @Override
    Localization.Command.Banlist localization(FPlayer fPlayer);

}
