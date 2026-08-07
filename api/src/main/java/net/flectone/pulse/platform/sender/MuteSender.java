package net.flectone.pulse.platform.sender;

import net.flectone.pulse.model.entity.FEntity;

/**
 * Sends mute notifications to players when they attempt to chat while muted.
 *
 * @author TheFaser
 * @since 1.6.0
 */
public interface MuteSender {

    /**
     * Checks if a player is muted and sends a mute notification.
     * Only sends messages to players, not other entities.
     *
     * @param entity the entity to check
     * @return true if player is muted and notification was sent, false otherwise
     */
    boolean sendIfMuted(FEntity entity);

}
