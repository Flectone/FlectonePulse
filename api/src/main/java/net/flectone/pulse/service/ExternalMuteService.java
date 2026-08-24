package net.flectone.pulse.service;

import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.value.ExternalModeration;

import java.util.Optional;
import java.util.UUID;

/**
 * Caches the mute another moderation plugin reports for a player
 *
 * @author TheFaser
 * @since 1.13.0
 */
public interface ExternalMuteService {

    /**
     * The external player mute, from cache when possible
     *
     * @param fPlayer the player
     * @return the mute, or empty if no integration reports one
     */
    Optional<ExternalModeration> get(FPlayer fPlayer);

    /**
     * Whether an integration reports this player as muted, from cache when possible
     *
     * @param fPlayer the player
     * @return true if an external mute applies
     */
    boolean isMuted(FPlayer fPlayer);

    /**
     * Invalidate cached answer for one player, so the next question reaches the integration again
     *
     * @param playerId the player
     */
    void invalidate(UUID playerId);

    /**
     * Invalidate every cached answer
     */
    void invalidateAll();

}
