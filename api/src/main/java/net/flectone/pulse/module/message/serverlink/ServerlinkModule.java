package net.flectone.pulse.module.message.serverlink;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleLocalization;

import java.util.UUID;

/**
 * Sends the server links shown in the client's pause menu.
 * @author TheFaser
 */
public interface ServerlinkModule extends ModuleLocalization {

    @Override
    Message.Serverlink config();

    @Override
    Permission.Message.Serverlink permission();

    @Override
    Localization.Message.Serverlink localization(FPlayer fPlayer);

    /**
     * Sends the configured links to a player.
     *
     * @param fPlayer the player
     */
    void sendLinks(FPlayer fPlayer);

    /**
     * Forgets cached links of a player.
     *
     * @param uuid the player
     */
    void remove(UUID uuid);

}
