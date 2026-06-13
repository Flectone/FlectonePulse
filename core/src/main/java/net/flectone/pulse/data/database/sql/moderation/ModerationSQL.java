package net.flectone.pulse.data.database.sql.moderation;

import net.flectone.pulse.data.database.sql.SQL;
import net.flectone.pulse.model.util.Moderation;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;

/**
 * SQL interface for moderation data operations in FlectonePulse.
 * Defines database queries for managing player moderation's.
 *
 * @author TheFaser
 * @since 0.9.0
 */
public interface ModerationSQL extends SQL {

    /**
     * Finds valid (non-expired) moderations for a specific player and type with pagination support.
     * Results are ordered by ID in descending order.
     *
     * @param playerId the player ID to search moderations for
     * @param type the moderation type to filter by
     * @param currentTime the current timestamp used to check expiration status
     * @param server the server ID (can be null for global search)
     * @param limit maximum number of results to return
     * @param offset number of results to skip for pagination
     * @return list of valid moderations matching the criteria
     */
    @SqlQuery("SELECT * FROM `fp_moderation` WHERE `player` = :player AND `type` = :type AND `valid` = true AND (`time` = -1 OR `time` > :currentTime) AND (:server IS NULL OR `server` IS NULL OR `server` = :server) ORDER BY `id` DESC LIMIT :limit OFFSET :offset")
    List<Moderation> findValidByPlayerAndType(@Bind("player") int playerId, @Bind("type") String type, @Bind("currentTime") long currentTime, @Bind("server") String server, @Bind("limit") int limit, @Bind("offset") int offset);

    /**
     * Finds valid (non-expired) moderations by type across all players with pagination support.
     * Results are ordered by ID in descending order.
     *
     * @param type the moderation type to filter by
     * @param currentTime the current timestamp used to check expiration status
     * @param server the server ID (can be null for global search)
     * @param limit maximum number of results to return
     * @param offset number of results to skip for pagination
     * @return list of valid moderations matching the criteria
     */
    @SqlQuery("SELECT * FROM `fp_moderation` WHERE `type` = :type AND `valid` = true AND (`time` = -1 OR `time` > :currentTime) AND (:server IS NULL OR `server` IS NULL OR `server` = :server) AND NOT `player` = -1 ORDER BY `id` DESC LIMIT :limit OFFSET :offset")
    List<Moderation> findValidByType(@Bind("type") String type, @Bind("currentTime") long currentTime, @Bind("server") String server, @Bind("limit") int limit, @Bind("offset") int offset);

    /**
     * Finds a single valid moderation entry by its unique identifier.
     * Checks that the moderation is valid and not expired, optionally filtered by server.
     *
     * @param currentTime the current timestamp used to check expiration status
     * @param server the server ID (can be null for global search)
     * @param id the unique moderation entry identifier
     * @return an Optional containing the moderation if found and valid, or empty otherwise
     */
    @SqlQuery("SELECT * FROM `fp_moderation` WHERE `valid` = true AND (`time` = -1 OR `time` > :currentTime) AND (:server IS NULL OR `server` IS NULL OR `server` = :server) AND `id` = :id")
    Optional<Moderation> findValidById(@Bind("currentTime") long currentTime, @Bind("server") String server, @Bind("id") int id);

    /**
     * Finds player names with valid moderation's by type.
     *
     * @param type the moderation type
     * @param currentTime the current timestamp for expiration check
     * @param server the server ID
     * @return list of player names
     */
    @SqlQuery("SELECT `p`.`name` FROM `fp_moderation` `m` JOIN `fp_player` `p` ON `p`.`id` = `m`.`player` WHERE `m`.`type` = :type AND `m`.`valid` = true AND (`m`.`time` = -1 OR `m`.`time` > :currentTime) AND (:server IS NULL OR `m`.`server` IS NULL OR `m`.`server` = :server) AND NOT `player` = -1")
    List<String> findValidPlayerNamesByType(@Bind("type") String type, @Bind("currentTime") long currentTime, @Bind("server") String server);

    /**
     * Counts the total number of valid moderations for a specific player and type.
     * Only includes non-expired moderations and can be filtered by server.
     *
     * @param playerId the player ID to count moderations for
     * @param type the moderation type to filter by
     * @param currentTime the current timestamp used to check expiration status
     * @param server the server ID (can be null for global count)
     * @return the count of valid moderations matching the criteria
     */
    @SqlQuery("SELECT COUNT(id) FROM `fp_moderation` WHERE `player` = :player AND `type` = :type AND `valid` = true AND (`time` = -1 OR `time` > :currentTime) AND (:server IS NULL OR `server` IS NULL OR `server` = :server)")
    int getTotalValidCountByPlayerAndType(@Bind("player") int playerId, @Bind("type") String type, @Bind("currentTime") long currentTime, @Bind("server") String server);

    /**
     * Counts the total number of valid moderations by type across all players.
     * Only includes non-expired moderations and can be filtered by server.
     *
     * @param type the moderation type to filter by
     * @param currentTime the current timestamp used to check expiration status
     * @param server the server ID (can be null for global count)
     * @return the count of valid moderations matching the criteria
     */
    @SqlQuery("SELECT COUNT(id) FROM `fp_moderation` WHERE `type` = :type AND `valid` = true AND (`time` = -1 OR `time` > :currentTime) AND (:server IS NULL OR `server` IS NULL OR `server` = :server) AND NOT `player` = -1")
    int getTotalValidCountByType(@Bind("type") String type, @Bind("currentTime") long currentTime, @Bind("server") String server);

    /**
     * Inserts a new moderation.
     *
     * @param playerId the player ID
     * @param date the timestamp when the moderation was applied
     * @param time the expiration timestamp (-1 for permanent)
     * @param reason the moderation reason
     * @param moderatorId the ID of the moderator
     * @param type the moderation type
     * @param server the server ID
     * @return the generated moderation ID
     */
    @GetGeneratedKeys("id")
    @SqlUpdate("INSERT INTO `fp_moderation` (`player`, `date`, `time`, `reason`, `moderator`, `type`, `valid`, `server`) VALUES (:player, :date, :time, :reason, :moderator, :type, true, :server)")
    int insert(@Bind("player") int playerId, @Bind("date") long date, @Bind("time") long time, @Bind("reason") String reason, @Bind("moderator") int moderatorId, @Bind("type") String type, @Bind("server") String server);

    /**
     * Invalidates a specific moderation entry by its ID.
     * Sets the valid flag to false, effectively removing it from active moderation lists.
     *
     * @param id the unique moderation entry identifier
     * @param server the server ID (can be null for global invalidation)
     */
    @SqlUpdate("UPDATE `fp_moderation` SET `valid` = false WHERE `id` = :id AND (:server IS NULL OR `server` IS NULL OR `server` = :server)")
    void invalidate(@Bind("id") int id, @Bind("server") String server);

    /**
     * Invalidates all moderations of a specific type for player.
     * Sets the valid flag to false for all matching entries, optionally filtered by server.
     *
     * @param playerId the player ID
     * @param type the moderation type to invalidate
     * @param server the server ID (can be null for global invalidation)
     */
    @SqlUpdate("UPDATE `fp_moderation` SET `valid` = false WHERE `player` = :player AND `type` = :type AND (:server IS NULL OR `server` IS NULL OR `server` = :server)")
    void invalidate(@Bind("player") int playerId, @Bind("type") String type, @Bind("server") String server);

}