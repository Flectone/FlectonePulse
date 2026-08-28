package net.flectone.pulse.module.command.unwarn;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;

/**
 * The /unwarn command.
 * @author TheFaser
 */
public interface UnwarnModule extends ModuleCommand {

    @Override
    Command.Unwarn config();

    @Override
    Permission.Command.Unwarn permission();

    @Override
    Localization.Command.Unwarn localization(FPlayer fPlayer);

    /**
     * Lifts a warning and announces it.
     *
     * @param fPlayer the moderator
     * @param target the warned player
     * @param id the warning id, or -1 for the most recent
     * @param reason the stated reason
     */
    void unwarn(FPlayer fPlayer, String target, int id, String reason);

}
