package net.flectone.pulse.data.database.sql.time;

import net.flectone.pulse.data.database.sql.SQL;
import net.flectone.pulse.exception.UnsupportedDatabaseOperationException;
import net.flectone.pulse.model.util.PlayTime;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;

/**
 * SQL interface for player playtime data operations in FlectonePulse.
 *
 * @author TheFaser
 * @since 1.9.0
 */
public interface TimeSQL extends SQL {

    @SqlUpdate("UPDATE `fp_time` SET `last` = :last, `total` = :total WHERE `player` = :player")
    void updateLastSeen(@Bind("last") double last, @Bind("total") double total, @Bind("player") int playerId);

    @SqlQuery("SELECT COUNT(*) FROM `fp_time`")
    int getTotalCount();

    @SqlQuery("SELECT * FROM `fp_time` WHERE `player` = :player")
    Optional<PlayTime> findByPlayer(@Bind("player") int playerId);

    @SqlQuery("SELECT * FROM `fp_time` ORDER BY `total` DESC LIMIT :limit OFFSET :offset")
    List<PlayTime> getAllPlayTimes(@Bind("limit") int limit, @Bind("offset") int offset);

    /**
     * Inserts a new playtime record or updates an existing one.
     *
     * @param playerId the player ID
     * @param first the timestamp of first join (only used if record is new)
     * @param last the timestamp of last seen
     * @param total the total playtime in seconds
     * @param sessions the number of sessions
     * @throws UnsupportedDatabaseOperationException if not overridden
     */
    default void upsert(@Bind("player") int playerId, @Bind("first") long first, @Bind("last") long last, @Bind("total") long total, @Bind("sessions") int sessions) {
        throw new UnsupportedDatabaseOperationException();
    }

}