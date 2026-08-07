package net.flectone.pulse.module.command.kick;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.util.constant.ModuleName;
import org.incendo.cloud.context.CommandContext;

/**
 * The /kick command.
 * @author TheFaser
 */
public interface KickModule extends ModuleCommand {

    @Override
    void onEnable();

    @Override
    void onDisable();

    @Override
    void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext);

    @Override
    ModuleName name();

    @Override
    Command.Kick config();

    @Override
    Permission.Command.Kick permission();

    @Override
    Localization.Command.Kick localization(FPlayer fPlayer);

    /**
     * Disconnects the player the entry was recorded against.
     *
     * @param kick the kick entry
     */
    void kick(Moderation kick);

}
