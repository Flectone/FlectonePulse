package net.flectone.pulse.checker;

import net.flectone.pulse.model.entity.FPlayer;

/**
 * Works out why a player may not speak, either an actual mute or a chat rule they just broke.
 * @author TheFaser
 */
public interface MuteChecker {

    /**
     * Checks whether the player is allowed to speak.
     *
     * @param fPlayer the player to check
     * @return what is stopping them, or {@link Status#NONE} if nothing is
     */
    Status check(FPlayer fPlayer);

    /**
     * Why a player cannot speak, either a local or external mute, a broken chat rule, or nothing at all.
     */
    enum Status {
        LOCAL,
        EXTERNAL,
        CAPS,
        FLOOD,
        NEWBIE,
        SWEAR,
        NONE
    }

}
