package net.flectone.pulse.module.message.tab.playerlist;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleLocalization;

/**
 * Rewrites the names shown in the tab list.
 * @author TheFaser
 */
public interface PlayerlistnameModule extends ModuleLocalization {

    @Override
    Message.Tab.Playerlistname config();

    @Override
    Permission.Message.Tab.Playerlistname permission();

    @Override
    Localization.Message.Tab.Playerlistname localization(FPlayer fPlayer);

    /**
     * Refreshes the tab list of every player.
     */
    void update();

    /**
     * Refreshes the tab list entry of one player.
     *
     * @param fPlayer the player
     */
    void update(FPlayer fPlayer);

}
