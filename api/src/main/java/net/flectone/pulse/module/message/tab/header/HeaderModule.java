package net.flectone.pulse.module.message.tab.header;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleListLocalization;

/**
 * Draws the rotating tab list header.
 * @author TheFaser
 */
public interface HeaderModule extends ModuleListLocalization {

    @Override
    Message.Tab.Header config();

    @Override
    Permission.Message.Tab.Header permission();

    @Override
    Localization.Message.Tab.Header localization(FPlayer fPlayer);

    /**
     * Shows the next header line to a player.
     *
     * @param fPlayer the player
     */
    void send(FPlayer fPlayer);

}
