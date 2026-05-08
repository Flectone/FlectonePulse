package net.flectone.pulse.data.repository;

import com.google.common.cache.Cache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.data.database.dao.ModerationDAO;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Moderation;
import org.incendo.cloud.type.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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

    private final @Named("moderation") Cache<Pair<UUID, Moderation.Type>, List<Moderation>> moderationCache;
    private final ModerationDAO moderationDAO;

    /**
     * Gets valid moderation's for a player with caching.
     *
     * @param player the player
     * @param type the moderation type
     * @param server the server ID
     * @return list of valid moderation's
     */
    public List<Moderation> getValid(@NonNull FPlayer player, Moderation.Type type, @Nullable String server) {
        try {
            Pair<UUID, Moderation.Type> key = Pair.of(player.uuid(), type);

            List<Moderation> cached = moderationCache.get(key, () -> moderationDAO.getValid(player, type, server));

            // cache should be saved only for backend server, i.e. who last used this method
            if (cached.stream().anyMatch(moderation -> !Objects.equals(server, moderation.server()))) {
                List<Moderation> newCached = moderationDAO.getValid(player, type, server);
                moderationCache.put(key, newCached);
                return newCached;
            }

            if (cached.stream().anyMatch(Moderation::isActive)) {
                return cached;
            }

            List<Moderation> valid = cached.stream()
                    .filter(Moderation::isActive)
                    .toList();

            moderationCache.put(key, valid);

            return valid;

        } catch (Exception _) {
            return Collections.emptyList();
        }
    }

    /**
     * Invalidates cache for a specific player and moderation type.
     *
     * @param playerId the player UUID
     * @param type the moderation type
     */
    public void invalidate(@NonNull UUID playerId, Moderation.Type type) {
        moderationCache.invalidate(Pair.of(playerId, type));
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
        moderationCache.asMap().keySet().removeIf(key ->
                key.first().equals(playerId)
        );
    }

    /**
     * Gets all moderation's for a player (including invalid ones).
     *
     * @param fPlayer the player
     * @param type the moderation type
     * @param server the server ID
     * @return list of all moderation's
     */
    public List<Moderation> get(@NonNull FPlayer fPlayer, Moderation.Type type, @Nullable String server) {
        return moderationDAO.get(fPlayer, type, server);
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
     * Gets all valid moderation's of a type.
     *
     * @param type the moderation type
     * @param server the server ID
     * @return list of valid moderation's
     */
    public List<Moderation> getValid(Moderation.Type type, @Nullable String server) {
        return moderationDAO.getValid(type, server);
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
     * Updates a moderation's validity.
     *
     * @param moderation the moderation record to update
     */
    public void updateValid(@NonNull Moderation moderation) {
        moderationDAO.updateValid(moderation);
    }
}