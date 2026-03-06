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
     * Records a player's join (first join or subsequent).
     *
     * @param fPlayer the player who joined
     */
    public void saveJoin(@NonNull FPlayer fPlayer) {
        if (fPlayer.isUnknown()) return;

        useTransaction(sql -> {
            long currentTime = System.currentTimeMillis();

            Optional<PlayTime> playTimeOptional = sql.findByPlayer(fPlayer.id());

            if (playTimeOptional.isPresent()) {
                sql.incrementSessions(currentTime, fPlayer.id());
            } else {
                sql.insert(fPlayer.id(), currentTime, currentTime, 0, 1);
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