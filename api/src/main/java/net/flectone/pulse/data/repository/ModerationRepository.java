package net.flectone.pulse.data.repository;

import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Moderation;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing moderation data in FlectonePulse.
 * Provides caching and retrieval of player moderation's.
 *
 * @author TheFaser
 * @since 0.8.1
 */
public interface ModerationRepository {

    /**
     * Retrieves valid (non-expired) moderations for a player with caching support.
     * Checks cache consistency across servers and refreshes if server mismatch is detected.
     * Only returns active moderations and updates cache accordingly.
     *
     * @param player the player to retrieve moderations for
     * @param type the moderation type to filter by
     * @param server the server ID (can be null for global search)
     * @param limit maximum number of results to return
     * @param offset number of results to skip for pagination
     * @return list of valid moderation actions, or empty list if an error occurs
     */
    List<Moderation> getValid(@NonNull FPlayer player, Moderation.Type type, @Nullable String server, int limit, int offset);

    /**
     * Invalidates cache for a specific player and moderation type.
     *
     * @param playerId the player UUID
     * @param type the moderation type
     */
    void invalidate(@NonNull UUID playerId, Moderation.Type type, @Nullable String server);

    /**
     * Invalidates all moderation cache.
     */
    void invalidateAll();

    /**
     * Invalidates cache for all moderation types for a player.
     *
     * @param playerId the player UUID
     */
    void invalidateAll(@NonNull UUID playerId);

    /**
     * Saves a new moderation.
     *
     * @param fTarget the target player
     * @param date the moderation date
     * @param time the expiration timestamp (-1 for permanent)
     * @param reason the moderation reason
     * @param moderatorID the moderator ID
     * @param type the moderation type
     * @param server the server ID
     * @return the created moderation
     */
    Moderation save(@NonNull FPlayer fTarget, long date, long time, String reason, int moderatorID, Moderation.Type type, String server);

    /**
     * Retrieves valid (non-expired) moderations by type across all players with pagination.
     * This method does not use caching and queries the database directly.
     *
     * @param type the moderation type to filter by
     * @param server the server ID (can be null for global search)
     * @param limit maximum number of results to return
     * @param offset number of results to skip for pagination
     * @return list of valid moderation actions matching the criteria
     */
    List<Moderation> getValid(Moderation.Type type, @Nullable String server, int limit, int offset);

    /**
     * Retrieves a single valid moderation entry by its unique identifier.
     * Checks that the moderation is valid and not expired.
     *
     * @param server the server ID (can be null for global search)
     * @param id the unique moderation entry identifier
     * @return an Optional containing the moderation if found and valid, or empty otherwise
     */
    Optional<Moderation> getValid(@Nullable String server, int id);

    /**
     * Gets names of players with valid moderation's of a type.
     *
     * @param type the moderation type
     * @param server the server ID
     * @return list of player names
     */
    List<String> getValidNames(Moderation.Type type, @Nullable String server);

    /**
     * Counts the total number of valid moderations for a specific player and type.
     * Only includes non-expired moderations.
     *
     * @param fPlayer the player to count moderations for
     * @param type the moderation type to filter by
     * @param server the server ID (can be null for global count)
     * @return the count of valid moderations matching the criteria
     */
    int getTotalValidCount(FPlayer fPlayer, Moderation.Type type, @Nullable String server);

    /**
     * Counts the total number of valid moderations by type across all players.
     * Only includes non-expired moderations.
     *
     * @param type the moderation type to filter by
     * @param server the server ID (can be null for global count)
     * @return the count of valid moderations matching the criteria
     */
    int getTotalValidCount(Moderation.Type type, @Nullable String server);

    /**
     * Invalidates a specific moderation entry by setting its valid flag to false.
     * This effectively removes it from active moderation lists without deleting the record.
     *
     * @param id the unique moderation entry identifier to invalidate
     * @param server the server ID (can be null for global invalidation)
     */
    void updateValid(int id, @Nullable String server);

    /**
     * Invalidates all player moderation entries of a specific type by setting their valid flag to false.
     * Can be filtered by server to target server-specific moderations only.
     *
     * @param playerId the player ID
     * @param type the moderation type to invalidate
     * @param server the server ID (can be null for global invalidation)
     */
    void updateValid(int playerId, Moderation.@NonNull Type type, @Nullable String server);

}
