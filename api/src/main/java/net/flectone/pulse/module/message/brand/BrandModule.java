package net.flectone.pulse.module.message.brand;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleListLocalization;

/**
 * Replaces the server brand shown in the client's debug screen.
 * @author TheFaser
 */
public interface BrandModule extends ModuleListLocalization {

    @Override
    Message.Brand config();

    @Override
    Permission.Message.Brand permission();

    @Override
    Localization.Message.Brand localization(FPlayer fPlayer);

    /**
     * Sends the current brand to a player.
     *
     * @param fPlayer the reader
     */
    void send(FPlayer fPlayer);

}
