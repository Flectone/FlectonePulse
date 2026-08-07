package net.flectone.pulse.module.command.anon;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.util.constant.ModuleName;
import org.incendo.cloud.context.CommandContext;

/**
 * The /anon command, which sends a message without revealing who wrote it.
 * @author TheFaser
 */
public interface AnonModule extends ModuleCommand {

    @Override
    void onEnable();

    @Override
    void onDisable();

    @Override
    void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext);

    @Override
    ModuleName name();

    @Override
    Command.Anon config();

    @Override
    Permission.Command.Anon permission();

    @Override
    Localization.Command.Anon localization(FPlayer fPlayer);

}
