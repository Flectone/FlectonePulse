package net.flectone.pulse.service;

import org.jspecify.annotations.NonNull;

import java.util.Set;
import java.util.UUID;

/**
 * Service for retrieving players near a given player.
 *
 * @author TheFaser
 * @since 1.15.0
 */
public interface NearbyService {

    /**
     * Rebuilds the spatial index of online players.
     */
    void reload();

    /**
     * Gets the UUIDs of players within the specified radius of the given player.
     *
     * @param player the UUID of the player to search around
     * @param radius the maximum distance in blocks
     * @return set of nearby player UUIDs, or an empty set if none are found
     */
    @NonNull Set<UUID> getNearbyPlayers(UUID player, double radius);

}