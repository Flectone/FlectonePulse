package net.flectone.pulse.module.message.update;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleLocalization;

/**
 * Checks for a newer plugin release and tells staff when one is out.
 * @author TheFaser
 */
public interface UpdateModule extends ModuleLocalization {

    @Override
    Message.Update config();

    @Override
    Permission.Message.Update permission();

    @Override
    Localization.Message.Update localization(FPlayer fPlayer);

    /**
     * Tells a player that a newer release is out.
     *
     * @param fPlayer the reader
     */
    void send(FPlayer fPlayer);

}
