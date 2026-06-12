package net.flectone.pulse.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.data.repository.PlaytimeRepository;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.PlayTime;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.util.file.FileFacade;
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
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PlaytimeService {

    private final FileFacade fileFacade;
    private final PlaytimeRepository playtimeRepository;
    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;

    /**
     * Initializes playtime tracking by migrating legacy data from platform adapters.
     * Only runs if playtime tracking is enabled and no existing records are found.
     * This serves as a migration for versions prior to 1.9.0 where playtime data
     * was not stored in the database.
     */
    public void initialize() {
        if (!isPlaytimeTracking()) return;

        // more like a migration for older versions below 1.9.0,
        // because this information was not in the database before.
        // and also we cannot add information about all players,
        // because players may not be in the database
        if (getPlayTimesCount() == 0) {
            fPlayerService.findAllFPlayers().forEach(fPlayer -> {
                if (fPlayer.isUnknown() || fPlayer.isConsole()) return;

                PlayTime platformPlayTime = platformPlayerAdapter.getPlayedTime(fPlayer);
                if (platformPlayTime == null) return;

                playtimeRepository.saveJoinSession(platformPlayTime);
            });
        }
    }

    /**
     * Invalidates cached playtime data for a specific player.
     * Only executes if playtime tracking is enabled.
     *
     * @param uuid the UUID of the player whose playtime cache should be invalidated
     */
    public void invalidate(@NonNull UUID uuid) {
        if (isPlaytimeTracking()) {
            playtimeRepository.invalidate(uuid);
        }
    }

    /**
     * Saves a player's AFK session status change and invalidates cache.
     * Only executes if playtime tracking is enabled.
     *
     * @param fPlayer the player whose AFK status is being updated
     * @param afk true if the player is going AFK, false if returning from AFK
     */
    public void saveAfkSession(FPlayer fPlayer, boolean afk) {
        if (isPlaytimeTracking()) {
            playtimeRepository.saveAfkSession(fPlayer, afk);
            playtimeRepository.invalidate(fPlayer.uuid());
        }
    }

    /**
     * Updates a player's join session when they connect to the server.
     * Saves the session and invalidates cached playtime data.
     * Only executes if playtime tracking is enabled.
     *
     * @param fPlayer the player whose join session is being updated
     */
    public void updateJoinSession(@NonNull FPlayer fPlayer) {
        if (isPlaytimeTracking()) {
            playtimeRepository.saveJoinSession(fPlayer);
            playtimeRepository.invalidate(fPlayer.uuid());
        }
    }

    /**
     * Updates a player's last seen timestamp when they disconnect from the server.
     * Saves the quit session and invalidates cached playtime data.
     * Only executes if playtime tracking is enabled.
     *
     * @param fPlayer the player whose last seen time is being updated
     */
    public void updateLastSession(@NonNull FPlayer fPlayer) {
        if (isPlaytimeTracking()) {
            playtimeRepository.saveLastSeen(fPlayer);
            playtimeRepository.invalidate(fPlayer.uuid());
        }
    }

    /**
     * Gets the playtime statistics for a specific player.
     * Returns null if playtime tracking is disabled.
     *
     * @param fPlayer the player to get playtime statistics for
     * @return the player's playtime statistics, or null if tracking is disabled or not found
     */
    public @Nullable PlayTime getPlayTime(FPlayer fPlayer) {
        return isPlaytimeTracking() ? playtimeRepository.getPlayTime(fPlayer) : null;
    }

    /**
     * Gets the total count of all playtime records in the database.
     * Returns -1 if playtime tracking is disabled.
     *
     * @return the total number of playtime records, or -1 if tracking is disabled
     */
    public int getPlayTimesCount() {
        return isPlaytimeTracking() ? playtimeRepository.getPlayTimesCount() : -1;
    }

    /**
     * Gets a paginated list of all playtime records from the database.
     * Returns an empty list if playtime tracking is disabled.
     *
     * @param limit the maximum number of records to retrieve
     * @param offset the number of records to skip before returning results
     * @return list of playtime records within the specified range, or empty list if tracking is disabled
     */
    @NonNull
    public List<PlayTime> getAllPlayTimes(int limit, int offset) {
        return isPlaytimeTracking() ? playtimeRepository.getAllPlayTimes(limit, offset) : List.of();
    }

    private boolean isPlaytimeTracking() {
        return fileFacade.config().database().usePlaytimeTracking();
    }

}
