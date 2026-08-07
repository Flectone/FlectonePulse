package net.flectone.pulse.module.message.join;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleLocalization;

/**
 * Announces that a player joined.
 * @author TheFaser
 */
public interface JoinModule extends ModuleLocalization {

    @Override
    ModuleName name();

    @Override
    Message.Join config();

    @Override
    Permission.Message.Join permission();

    @Override
    Localization.Message.Join localization(FPlayer fPlayer);

    /**
     * Whether the proxy announces joins instead of this server, which avoids a duplicate message.
     *
     * @return true if the proxy handles it
     */
    boolean isProxyMode();

    /**
     * Announces a join.
     *
     * @param fPlayer the joining player
     * @param fakeMessage whether this is a stand-in produced by vanishing rather than a real join
     */
    void send(FPlayer fPlayer, boolean fakeMessage);

    /**
     * Announces a join.
     *
     * @param fPlayer the joining player
     * @param fakeMessage whether this is a stand-in produced by vanishing
     * @param checkProxyMode whether to let the proxy announce it instead, to avoid a duplicate
     */
    void send(FPlayer fPlayer, boolean fakeMessage, boolean checkProxyMode);

}
