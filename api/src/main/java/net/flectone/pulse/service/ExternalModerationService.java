package net.flectone.pulse.service;

import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.value.ExternalModeration;
import net.flectone.pulse.model.value.Moderation;

import java.util.Optional;
import java.util.UUID;

/**
 * Caches external moderation reports from other plugins for a player
 *
 * @author TheFaser
 * @since 1.13.0
 */
public interface ExternalModerationService {

    /**
     * The external moderation for a player, from cache when possible
     *
     * @param fPlayer the player
     * @param type the moderation type
     * @return the moderation, or empty if no integration reports one
     */
    Optional<ExternalModeration> get(FPlayer fPlayer, Moderation.Type type);

    /**
     * Whether an integration reports this moderation type for this player, from cache when possible
     *
     * @param fPlayer the player
     * @param type the moderation type
     * @return true if an external moderation applies
     */
    boolean isPresent(FPlayer fPlayer, Moderation.Type type);

    /**
     * Invalidate cached answer for one player and moderation type, so the next question reaches the integration again
     *
     * @param uuid the player
     * @param type the moderation type
     */
    void invalidate(UUID uuid, Moderation.Type type);

    /**
     * Invalidate every cached answer
     */
    void invalidateAll();

    /**
     * Key identifying one player and one external moderation type
     *
     * @param player the player
     * @param type the moderation type
     */
    record ExternalModerationKey(
            UUID player,
            Moderation.Type type
    ) {
    }
}