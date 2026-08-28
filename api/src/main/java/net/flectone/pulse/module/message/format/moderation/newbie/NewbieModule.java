package net.flectone.pulse.module.message.format.moderation.newbie;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.value.ExternalModeration;
import net.flectone.pulse.module.ModuleLocalization;

/**
 * Keeps players quiet until they have been on the server long enough.
 * @author TheFaser
 */
public interface NewbieModule extends ModuleLocalization {

    @Override
    Message.Format.Moderation.Newbie config();

    @Override
    Permission.Message.Format.Moderation.Newbie permission();

    @Override
    Localization.Message.Format.Moderation.Newbie localization(FPlayer fPlayer);

    /**
     * Whether a player is still too new to speak.
     *
     * @param fPlayer the player
     * @return true if they are held back
     */
    boolean isNewBie(FPlayer fPlayer);

    /**
     * The stand-in punishment describing why a newcomer may not speak yet.
     *
     * @param fPlayer the player
     * @return the punishment, or null if they may speak
     */
    ExternalModeration getModeration(FPlayer fPlayer);

}
