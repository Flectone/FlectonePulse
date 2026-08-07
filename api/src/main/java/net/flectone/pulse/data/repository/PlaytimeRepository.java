package net.flectone.pulse.data.repository;

import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.PlayTime;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Repository for managing player playtime statistics and session tracking in FlectonePulse.
 * Handles saving join/quit sessions and retrieving playtime data
 * with caching support using Guava Cache.
 *
 * @author TheFaser
 * @since 1.10.1
 * @see PlayTime
 */
public interface PlaytimeRepository {

    /**
     * Saves a player's join session when they connect to the server.
     *
     * @param fPlayer the player whose join session is being saved
     */
    void saveJoinSession(FPlayer fPlayer);

    /**
     * Saves a playtime session directly.
     *
     * @param playTime the playtime session to save
     */
    void saveJoinSession(PlayTime playTime);

    /**
     * Saves a player's AFK session status change.
     *
     * @param fPlayer the player whose AFK status is being updated
     * @param afk true if the player is going AFK, false if returning from AFK
     */
    void saveAfkSession(FPlayer fPlayer, boolean afk);

    /**
     * Saves a player's last seen timestamp when they disconnect from the server.
     *
     * @param fPlayer the player whose last seen time is being recorded
     */
    void saveLastSeen(FPlayer fPlayer);

    /**
     * Gets the playtime statistics for a specific player with cache support.
     * Returns cached playtime if available, otherwise loads from database and caches the result.
     *
     * @param fPlayer the player to get playtime statistics for
     * @return the player's playtime statistics, or null if not found
     */
    @Nullable PlayTime getPlayTime(FPlayer fPlayer);

    /**
     * Gets the total count of all playtime records in the database.
     *
     * @return the total number of playtime records
     */
    int getPlayTimesCount();

    /**
     * Gets a paginated list of all playtime records from the database.
     *
     * @param limit the maximum number of records to retrieve
     * @param offset the number of records to skip before returning results
     * @return list of playtime records within the specified range
     */
    List<PlayTime> getAllPlayTimes(int limit, int offset);

    /**
     * Invalidates cached playtime statistics for a player.
     *
     * @param uuid the UUID of the player whose playtime cache should be cleared
     */
    void invalidate(UUID uuid);

}
