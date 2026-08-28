package net.flectone.pulse.module.command.kick;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.value.Moderation;
import net.flectone.pulse.module.ModuleCommand;

/**
 * The /kick command.
 * @author TheFaser
 */
public interface KickModule extends ModuleCommand {

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
