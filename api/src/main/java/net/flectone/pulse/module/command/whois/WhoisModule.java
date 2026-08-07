package net.flectone.pulse.module.command.whois;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.util.constant.ModuleName;
import org.incendo.cloud.context.CommandContext;

/**
 * The /whois command, which shows what the plugin knows about a player.
 * @author TheFaser
 */
public interface WhoisModule extends ModuleCommand {

    @Override
    void onEnable();

    @Override
    void onDisable();

    @Override
    void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext);

    @Override
    ModuleName name();

    @Override
    Command.Whois config();

    @Override
    Permission.Command.Whois permission();

    @Override
    Localization.Command.Whois localization(FPlayer fPlayer);

}
