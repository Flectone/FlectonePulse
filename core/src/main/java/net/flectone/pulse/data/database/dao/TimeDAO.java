package net.flectone.pulse.data.database.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.data.database.Database;
import net.flectone.pulse.data.database.sql.TimeSQL;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.PlayTime;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for player playtime data in FlectonePulse.
 * Handles playtime tracking and statistics for players.
 *
 * @author TheFaser
 * @since 1.9.0
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TimeDAO implements BaseDAO<TimeSQL> {

    private final Database database;

    @Override
    public Database database() {
        return database;
    }

    @Override
    public Class<TimeSQL> sqlClass() {
        return TimeSQL.class;
    }

    /**
     * Records a player's join
     * If the player already exists, increments their session count;
     * otherwise inserts a new playtime record.
     *
     * @param fPlayer the player who joined
     */
    public void saveJoin(@NonNull FPlayer fPlayer) {
        if (fPlayer.isUnknown()) return;

        saveSession(new PlayTime(-1, fPlayer.id(), System.currentTimeMillis(), System.currentTimeMillis(), 0, 1));
    }

    /**
     * Records a player's session
     * If the player already exists, increments their session count;
     * otherwise inserts a new playtime record.
     *
     * @param playTime the playtime data to save
     */
    public void saveSession(@NonNull PlayTime playTime) {
        if (playTime.id() != -1) return;

        useTransaction(sql -> {
            Optional<PlayTime> playTimeOptional = sql.findByPlayer(playTime.playerId());

            if (playTimeOptional.isPresent()) {
                sql.incrementSessions(playTime.last(), playTime.playerId());
            } else {
                sql.insert(playTime.playerId(), playTime.first(), playTime.last(), playTime.total(), playTime.sessions());
            }
        });
    }

    /**
     * Updates total playtime with AFK.
     * If the player is entering AFK, adds elapsed time to total and invert last seen.
     * If the player is returning from AFK, updates the last seen timestamp.
     *
     * @param fPlayer the player whose AFK status is being updated
     * @param afk true if the player is going AFK, false if returning
     */
    public void saveAfk(@NonNull FPlayer fPlayer, boolean afk) {
        if (fPlayer.isUnknown()) return;

        useTransaction(sql -> {
            long currentTime = System.currentTimeMillis();

            Optional<PlayTime> playTimeOptional = sql.findByPlayer(fPlayer.id());
            if (playTimeOptional.isEmpty()) return;

            PlayTime playTime = playTimeOptional.get();
            if (afk) {
                long newTotal = playTime.total() + (currentTime - playTime.last());
                sql.updateLastSeen(playTime.last() * -1.0, newTotal, fPlayer.id());
            } else {
                sql.updateLastSeen(currentTime, playTime.total(), fPlayer.id());
            }
        });
    }

    /**
     * Records a player's quit and updates total playtime.
     *
     * @param fPlayer the player who quit
     */
    public void saveQuit(@NonNull FPlayer fPlayer) {
        if (fPlayer.isUnknown()) return;

        useTransaction(sql -> {
            long currentTime = System.currentTimeMillis();

            Optional<PlayTime> playTimeOptional = sql.findByPlayer(fPlayer.id());
            if (playTimeOptional.isEmpty()) return;

            PlayTime playTime = playTimeOptional.get();
            if (playTime.last() < 0) return;

            long newTotal = playTime.total() + (currentTime - playTime.last());

            sql.updateLastSeen(currentTime, newTotal, fPlayer.id());
        });
    }

    /**
     * Gets playtime record for a player.
     *
     * @param fPlayer the player
     * @return optional containing the playtime record
     */
    public @NonNull Optional<PlayTime> getByPlayer(@NonNull FPlayer fPlayer) {
        if (fPlayer.isUnknown()) return Optional.empty();

        return withHandle(sql -> sql.findByPlayer(fPlayer.id()));
    }

    /**
     * Gets total number of playtime records.
     *
     * @return total count
     */
    public int getTotalCount() {
        return withHandle(TimeSQL::getTotalCount);
    }

    /**
     * Gets all playtime records with player names.
     *
     * @return list of playtime records
     */
    public @NonNull List<PlayTime> getAllPlayTimes(int limit, int offset) {
        return withHandle(timeSQL -> timeSQL.getAllPlayTimes(limit, offset));
    }

}