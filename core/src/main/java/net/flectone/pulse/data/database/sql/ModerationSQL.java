package net.flectone.pulse.data.database.sql;

import net.flectone.pulse.model.util.Moderation;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

/**
 * SQL interface for moderation data operations in FlectonePulse.
 * Defines database queries for managing player moderation's.
 *
 * @author TheFaser
 * @since 0.9.0
 */
public interface ModerationSQL extends SQL {

    /**
     * Finds moderation's for a player by type.
     *
     * @param playerId the player ID
     * @param type the moderation type
     * @return list of moderation's
     */
    @SqlQuery("SELECT * FROM `fp_moderation` WHERE `player` = :player AND `type` = :type")
    List<Moderation> findByPlayerAndType(@Bind("player") int playerId, @Bind("type") int type);

    /**
     * Finds valid moderation's for a player by type.
     *
     * @param playerId the player ID
     * @param type the moderation type
     * @param currentTime the current timestamp for expiration check
     * @return list of valid moderation's
     */
    @SqlQuery("SELECT * FROM `fp_moderation` WHERE `player` = :player AND `type` = :type AND `valid` = true AND (`time` = -1 OR `time` > :currentTime)")
    List<Moderation> findValidByPlayerAndType(@Bind("player") int playerId, @Bind("type") int type, @Bind("currentTime") long currentTime);

    /**
     * Finds valid moderation's by type.
     *
     * @param type the moderation type
     * @param currentTime the current timestamp for expiration check
     * @return list of valid moderation's
     */
    @SqlQuery("SELECT * FROM `fp_moderation` WHERE `type` = :type AND `valid` = true AND (`time` = -1 OR `time` > :currentTime)")
    List<Moderation> findValidByType(@Bind("type") int type, @Bind("currentTime") long currentTime);

    /**
     * Finds player names with valid moderation's by type.
     *
     * @param type the moderation type
     * @param currentTime the current timestamp for expiration check
     * @return list of player names
     */
    @SqlQuery("SELECT `p`.`name` FROM `fp_moderation` `m` JOIN `fp_player` `p` ON `p`.`id` = `m`.`player` WHERE `m`.`type` = :type AND `m`.`valid` = true AND (`m`.`time` = -1 OR `m`.`time` > :currentTime)")
    List<String> findValidPlayerNamesByType(@Bind("type") int type, @Bind("currentTime") long currentTime);

    /**
     * Inserts a new moderation.
     *
     * @param playerId the player ID
     * @param date the timestamp when the moderation was applied
     * @param time the expiration timestamp (-1 for permanent)
     * @param reason the moderation reason
     * @param moderatorId the ID of the moderator
     * @param type the moderation type
     * @return the generated moderation ID
     */
    @GetGeneratedKeys("id")
    @SqlUpdate("INSERT INTO `fp_moderation` (`player`, `date`, `time`, `reason`, `moderator`, `type`, `valid`) VALUES (:player, :date, :time, :reason, :moderator, :type, true)")
    int insert(@Bind("player") int playerId, @Bind("date") long date, @Bind("time") long time, @Bind("reason") String reason, @Bind("moderator") int moderatorId, @Bind("type") int type);

    /**
     * Invalidates a moderation.
     *
     * @param id the moderation ID
     */
    @SqlUpdate("UPDATE `fp_moderation` SET `valid` = false WHERE `id` = :id")
    void invalidate(@Bind("id") int id);

}