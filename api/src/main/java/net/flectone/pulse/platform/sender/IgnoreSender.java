package net.flectone.pulse.platform.sender;

import net.flectone.pulse.model.entity.FPlayer;

/**
 * Sends ignore-related messages when players ignore each other.
 *
 * @author TheFaser
 * @since 1.6.0
 */
public interface IgnoreSender {

    /**
     * Checks if two players ignore each other and sends notification to sender.
     *
     * @param fPlayer the player attempting to send a message (receives notification)
     * @param fTarget the target player
     * @return true if either player ignores the other, false otherwise
     */
    boolean sendIfIgnored(FPlayer fPlayer, FPlayer fTarget);

}
