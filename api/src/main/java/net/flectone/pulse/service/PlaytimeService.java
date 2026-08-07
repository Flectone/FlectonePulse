package net.flectone.pulse.service;

import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.value.PlayTime;
import net.flectone.pulse.persistence.repository.PlaytimeRepository;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Service for managing player playtime tracking and statistics.
 * Handles initialization, session updates, and retrieval of playtime data.
 * Playtime tracking can be enabled or disabled via configuration.
 *
 * @see PlayTime
 * @see PlaytimeRepository
 *
 * @author TheFaser
 * @since 1.10.1
 */
public interface PlaytimeService {

    /**
     * Initializes playtime tracking by migrating legacy data from platform adapters.
     * Only runs if playtime tracking is enabled and no existing records are found.
     * This serves as a migration for versions prior to 1.9.0 where playtime data
     * was not stored in the database.
     */
    void initialize();

    /**
     * Invalidates cached playtime data for a specific player.
     * Only executes if playtime tracking is enabled.
     *
     * @param uuid the UUID of the player whose playtime cache should be invalidated
     */
    void invalidate(@NonNull UUID uuid);

    /**
     * Saves a player's AFK session status change and invalidates cache.
     * Only executes if playtime tracking is enabled.
     *
     * @param fPlayer the player whose AFK status is being updated
     * @param afk true if the player is going AFK, false if returning from AFK
     */
    void saveAfkSession(FPlayer fPlayer, boolean afk);

    /**
     * Updates a player's join session when they connect to the server.
     * Saves the session and invalidates cached playtime data.
     * Only executes if playtime tracking is enabled.
     *
     * @param fPlayer the player whose join session is being updated
     */
    void updateJoinSession(@NonNull FPlayer fPlayer);

    /**
     * Updates a player's last seen timestamp when they disconnect from the server.
     * Saves the quit session and invalidates cached playtime data.
     * Only executes if playtime tracking is enabled.
     *
     * @param fPlayer the player whose last seen time is being updated
     */
    void updateLastSession(@NonNull FPlayer fPlayer);

    /**
     * Gets the playtime statistics for a specific player.
     * Returns null if playtime tracking is disabled.
     *
     * @param fPlayer the player to get playtime statistics for
     * @return the player's playtime statistics, or null if tracking is disabled or not found
     */
    @Nullable PlayTime getPlayTime(FPlayer fPlayer);

    /**
     * Gets the total count of all playtime records in the database.
     * Returns -1 if playtime tracking is disabled.
     *
     * @return the total number of playtime records, or -1 if tracking is disabled
     */
    int getPlayTimesCount();

    /**
     * Gets a paginated list of all playtime records from the database.
     * Returns an empty list if playtime tracking is disabled.
     *
     * @param limit the maximum number of records to retrieve
     * @param offset the number of records to skip before returning results
     * @return list of playtime records within the specified range, or empty list if tracking is disabled
     */
    @NonNull
    List<PlayTime> getAllPlayTimes(int limit, int offset);

}
