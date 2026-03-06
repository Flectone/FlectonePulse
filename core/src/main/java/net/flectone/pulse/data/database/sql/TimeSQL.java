package net.flectone.pulse.data.database.sql;

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

    /**
     * Inserts a new play time for a player.
     *
     * @param playerId the ID of the player
     * @param first the timestamp of first join
     * @param last the timestamp of last seen
     * @param total the total playtime in seconds
     * @param sessions the number of sessions
     */
    @SqlUpdate("INSERT INTO `fp_time` (`player`, `first`, `last`, `total`, `sessions`) VALUES (:player, :first, :last, :total, :sessions)")
    int insert(@Bind("player") int playerId, @Bind("first") long first, @Bind("last") long last, @Bind("total") long total, @Bind("sessions") int sessions);

    /**
     * Updates player's last seen and total time.
     *
     * @param last the new last seen timestamp
     * @param total the new total playtime
     * @param sessions the updated session count
     * @param playerId the ID of the player
     */
    @SqlUpdate("UPDATE `fp_time` SET `last` = :last, `total` = :total, `sessions` = :sessions WHERE `player` = :player")
    int update(@Bind("last") double last, @Bind("total") double total, @Bind("sessions") int sessions, @Bind("player") int playerId);

    /**
     * Updates only the last seen timestamp.
     *
     * @param last the new last seen timestamp
     * @param total the new total playtime
     * @param playerId the ID of the player
     * @return the number of rows updated
     */
    @SqlUpdate("UPDATE `fp_time` SET `last` = :last, `total` = :total WHERE `player` = :player")
    int updateLastSeen(@Bind("last") double last, @Bind("total") double total, @Bind("player") int playerId);

    /**
     * Increments session count
     *
     * @param playerId the ID of the player
     * @return the number of rows updated
     */
    @SqlUpdate("UPDATE `fp_time` SET `sessions` = `sessions` + 1, `last` = :last WHERE `player` = :player")
    int incrementSessions(@Bind("last") double last, @Bind("player") int playerId);

    /**
     * Gets total count of playtime records.
     *
     * @return total number of records
     */
    @SqlQuery("SELECT COUNT(*) FROM `fp_time`")
    int getTotalCount();

    /**
     * Finds playtime record by player ID.
     *
     * @param playerId the ID of the player
     * @return optional containing the playtime record if found
     */
    @SqlQuery("SELECT * FROM `fp_time` WHERE `player` = :player")
    Optional<PlayTime> findByPlayer(@Bind("player") int playerId);

    /**
     * Gets all playtime records with pagination.
     *
     * @param limit maximum number of records to return
     * @param offset offset for pagination
     * @return list of playtime records
     */
    @SqlQuery("SELECT * FROM `fp_time` ORDER BY `total` DESC LIMIT :limit OFFSET :offset")
    List<PlayTime> getAllPlayTimes(@Bind("limit") int limit, @Bind("offset") int offset);

}