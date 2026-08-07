package net.flectone.pulse.module.message.quit;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleLocalization;

/**
 * Announces that a player left.
 * @author TheFaser
 */
public interface QuitModule extends ModuleLocalization {

    @Override
    ModuleName name();

    @Override
    Message.Quit config();

    @Override
    Permission.Message.Quit permission();

    @Override
    Localization.Message.Quit localization(FPlayer fPlayer);

    /**
     * Whether the proxy announces quits instead of this server, which avoids a duplicate message.
     *
     * @return true if the proxy handles it
     */
    boolean isProxyMode();

    /**
     * Announces a quit.
     *
     * @param fPlayer the leaving player
     * @param fakeMessage whether this is a stand-in produced by vanishing rather than a real quit
     */
    void send(FPlayer fPlayer, boolean fakeMessage);

    /**
     * Announces a quit.
     *
     * @param fPlayer the leaving player
     * @param fakeMessage whether this is a stand-in produced by vanishing
     * @param vanished whether the player was hidden at the time
     */
    void send(FPlayer fPlayer, boolean fakeMessage, boolean vanished);

}
