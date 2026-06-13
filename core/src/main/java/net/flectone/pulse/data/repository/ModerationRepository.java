package net.flectone.pulse.data.repository;

import com.google.common.cache.Cache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.data.database.dao.ModerationDAO;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Moderation;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Repository for managing moderation data in FlectonePulse.
 * Provides caching and retrieval of player moderation's.
 *
 * @author TheFaser
 * @since 0.8.1
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ModerationRepository {

    private final @Named("moderation") Cache<UUID, Map<String, List<Moderation>>> moderationCache;
    private final ModerationDAO moderationDAO;

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
    public List<Moderation> getValid(@NonNull FPlayer player, Moderation.Type type, @Nullable String server, int limit, int offset) {
        Map<String, List<Moderation>> playerModerations = moderationCache.getIfPresent(player.uuid());
        if (playerModerations == null) {
            playerModerations = new ConcurrentHashMap<>();
            moderationCache.put(player.uuid(), playerModerations);
        }

        String typeServerKey = type.name() + server;
        List<Moderation> moderations = playerModerations.get(typeServerKey);
        if (moderations == null) {
            // get from database
            moderations = moderationDAO.getValid(player, type, server, limit, offset);

            // add to cache
            playerModerations.put(typeServerKey, moderations);

            return moderations;
        }

        if (moderations.stream().allMatch(Moderation::isActive)) {
            return moderations;
        }

        List<Moderation> valid = moderations.stream()
                .filter(Moderation::isActive)
                .toList();

        playerModerations.put(typeServerKey, valid);

        return valid;
    }

    /**
     * Invalidates cache for a specific player and moderation type.
     *
     * @param playerId the player UUID
     * @param type the moderation type
     */
    public void invalidate(@NonNull UUID playerId, Moderation.Type type, @Nullable String server) {
        Map<String, List<Moderation>> playerModerations = moderationCache.getIfPresent(playerId);
        if (playerModerations == null) return;

        playerModerations.remove(type.name() + server);

        // remove cache key if map empty
        if (playerModerations.isEmpty()) {
            moderationCache.invalidate(playerId);
        }
    }

    /**
     * Invalidates all moderation cache.
     */
    public void invalidateAll() {
        moderationCache.invalidateAll();
    }

    /**
     * Invalidates cache for all moderation types for a player.
     *
     * @param playerId the player UUID
     */
    public void invalidateAll(@NonNull UUID playerId) {
        moderationCache.invalidate(playerId);
    }

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
    public Moderation save(@NonNull FPlayer fTarget, long date, long time, String reason, int moderatorID, Moderation.Type type, String server) {
        return moderationDAO.insert(fTarget, date, time, reason, moderatorID, type, server);
    }

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
    public List<Moderation> getValid(Moderation.Type type, @Nullable String server, int limit, int offset) {
        return moderationDAO.getValid(type, server, limit, offset);
    }

    /**
     * Retrieves a single valid moderation entry by its unique identifier.
     * Checks that the moderation is valid and not expired.
     *
     * @param server the server ID (can be null for global search)
     * @param id the unique moderation entry identifier
     * @return an Optional containing the moderation if found and valid, or empty otherwise
     */
    public Optional<Moderation> getValid(@Nullable String server, int id) {
        return moderationDAO.getValidById(server, id);
    }

    /**
     * Gets names of players with valid moderation's of a type.
     *
     * @param type the moderation type
     * @param server the server ID
     * @return list of player names
     */
    public List<String> getValidNames(Moderation.Type type, @Nullable String server) {
        return moderationDAO.getValidPlayersNames(type, server);
    }

    /**
     * Counts the total number of valid moderations for a specific player and type.
     * Only includes non-expired moderations.
     *
     * @param fPlayer the player to count moderations for
     * @param type the moderation type to filter by
     * @param server the server ID (can be null for global count)
     * @return the count of valid moderations matching the criteria
     */
    public int getTotalValidCount(FPlayer fPlayer, Moderation.Type type, @Nullable String server) {
        return moderationDAO.getTotalValidCount(fPlayer, type, server);
    }

    /**
     * Counts the total number of valid moderations by type across all players.
     * Only includes non-expired moderations.
     *
     * @param type the moderation type to filter by
     * @param server the server ID (can be null for global count)
     * @return the count of valid moderations matching the criteria
     */
    public int getTotalValidCount(Moderation.Type type, @Nullable String server) {
        return moderationDAO.getTotalValidCount(type, server);
    }

    /**
     * Invalidates a specific moderation entry by setting its valid flag to false.
     * This effectively removes it from active moderation lists without deleting the record.
     *
     * @param id the unique moderation entry identifier to invalidate
     * @param server the server ID (can be null for global invalidation)
     */
    public void updateValid(int id, @Nullable String server) {
        moderationDAO.updateValid(id, server);
    }

    /**
     * Invalidates all player moderation entries of a specific type by setting their valid flag to false.
     * Can be filtered by server to target server-specific moderations only.
     *
     * @param playerId the player ID
     * @param type the moderation type to invalidate
     * @param server the server ID (can be null for global invalidation)
     */
    public void updateValid(int playerId, Moderation.@NonNull Type type, @Nullable String server) {
        moderationDAO.updateValid(playerId, type, server);
    }

}