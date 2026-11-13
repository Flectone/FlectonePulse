package net.flectone.pulse.module.message.tab.footer;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleListLocalization;

/**
 * Draws the rotating tab list footer.
 * @author TheFaser
 */
public interface FooterModule extends ModuleListLocalization {

    @Override
    Message.Tab.Footer config();

    @Override
    Permission.Message.Tab.Footer permission();

    @Override
    Localization.Message.Tab.Footer localization(FPlayer fPlayer);

    /**
     * Shows the next footer line to a player.
     *
     * @param fPlayer the player
     */
    void send(FPlayer fPlayer);

}
