package net.flectone.pulse.data.repository;

import com.google.common.cache.Cache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.data.database.dao.TimeDAO;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.PlayTime;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing player playtime statistics and session tracking in FlectonePulse.
 * Handles saving join/quit sessions and retrieving playtime data
 * with caching support using Guava Cache.
 *
 * @author TheFaser
 * @since 1.10.1
 * @see TimeDAO
 * @see PlayTime
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PlaytimeRepository {

    private final @Named("playtime") Cache<UUID, PlayTime> playTimeCache;

    private final TimeDAO timeDAO;

    /**
     * Saves a player's join session when they connect to the server.
     *
     * @param fPlayer the player whose join session is being saved
     */
    public void saveJoinSession(FPlayer fPlayer) {
        timeDAO.saveJoin(fPlayer);
    }

    /**
     * Saves a playtime session directly.
     *
     * @param playTime the playtime session to save
     */
    public void saveJoinSession(PlayTime playTime) {
        timeDAO.saveSession(playTime);
    }

    /**
     * Saves a player's AFK session status change.
     *
     * @param fPlayer the player whose AFK status is being updated
     * @param afk true if the player is going AFK, false if returning from AFK
     */
    public void saveAfkSession(FPlayer fPlayer, boolean afk) {
        timeDAO.saveAfk(fPlayer, afk, getPlayTime(fPlayer));
    }

    /**
     * Saves a player's last seen timestamp when they disconnect from the server.
     *
     * @param fPlayer the player whose last seen time is being recorded
     */
    public void saveLastSeen(FPlayer fPlayer) {
        timeDAO.saveQuit(fPlayer, getPlayTime(fPlayer));
    }

    /**
     * Gets the playtime statistics for a specific player with cache support.
     * Returns cached playtime if available, otherwise loads from database and caches the result.
     *
     * @param fPlayer the player to get playtime statistics for
     * @return the player's playtime statistics, or null if not found
     */
    public @Nullable PlayTime getPlayTime(FPlayer fPlayer) {
        PlayTime cached = playTimeCache.getIfPresent(fPlayer.uuid());
        if (cached != null) return cached;

        Optional<PlayTime> playTime = timeDAO.getByPlayer(fPlayer);
        playTime.ifPresent(time -> playTimeCache.put(fPlayer.uuid(), time));

        return playTime.orElse(null);
    }

    /**
     * Gets the total count of all playtime records in the database.
     *
     * @return the total number of playtime records
     */
    public int getPlayTimesCount() {
        return timeDAO.getTotalCount();
    }

    /**
     * Gets a paginated list of all playtime records from the database.
     *
     * @param limit the maximum number of records to retrieve
     * @param offset the number of records to skip before returning results
     * @return list of playtime records within the specified range
     */
    public List<PlayTime> getAllPlayTimes(int limit, int offset) {
        return timeDAO.getAllPlayTimes(limit, offset);
    }

    /**
     * Invalidates cached playtime statistics for a player.
     *
     * @param uuid the UUID of the player whose playtime cache should be cleared
     */
    public void invalidate(UUID uuid) {
        playTimeCache.invalidate(uuid);
    }

}
