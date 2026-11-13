package net.flectone.pulse.module.message.status.players;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleLocalization;

import java.util.List;

/**
 * Controls the player count and the player sample shown in the server list.
 * @author TheFaser
 */
public interface PlayersModule extends ModuleLocalization {

    @Override
    Message.Status.Players config();

    @Override
    Permission.Message.Status.Players permission();

    @Override
    Localization.Message.Status.Players localization(FPlayer fPlayer);

    /**
     * Whether a player is allowed to join with the configured online limit.
     *
     * @param fPlayer the player
     * @return true if the player may join
     */
    boolean isAllowed(FPlayer fPlayer);

    /**
     * The maximum player count shown to a player.
     *
     * @param fPlayer the player
     * @return the maximum
     */
    int getMaxOnline(FPlayer fPlayer);

    /**
     * The online player count shown to a player.
     *
     * @param fPlayer the player
     * @return the count
     */
    int getOnline(FPlayer fPlayer);

    /**
     * The sample lines shown under the player count.
     *
     * @param fPlayer the player
     * @return the sample lines, or null if the module is disabled
     */
    List<Localization.Message.Status.Players.Sample> getSamples(FPlayer fPlayer);

}
