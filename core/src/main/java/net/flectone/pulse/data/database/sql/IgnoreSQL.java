package net.flectone.pulse.data.database.sql;

import net.flectone.pulse.module.command.ignore.model.Ignore;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;

/**
 * SQL interface for ignore data operations in FlectonePulse.
 * Defines database queries for managing player ignore relationships.
 *
 * @author TheFaser
 * @since 0.9.0
 */
public interface IgnoreSQL extends SQL {

    /**
     * Inserts a new ignore relationship.
     *
     * @param date the timestamp when the ignore was created
     * @param initiatorId the ID of the player who is ignoring
     * @param targetId the ID of the player being ignored
     * @return the generated ignore ID
     */
    @GetGeneratedKeys("id")
    @SqlUpdate("INSERT INTO `fp_ignore` (`date`, `initiator`, `target`) VALUES (:date, :initiator, :target)")
    int insert(@Bind("date") long date, @Bind("initiator") int initiatorId, @Bind("target") int targetId);

    /**
     * Updates an existing ignore relationship.
     *
     * @param date the new timestamp
     * @param initiatorId the ID of the player who is ignoring
     * @param targetId the ID of the player being ignored
     * @return the number of rows updated
     */
    @SqlUpdate("UPDATE `fp_ignore` SET `date` = :date, `valid` = true WHERE `initiator` = :initiator AND `target` = :target")
    int update(@Bind("date") long date, @Bind("initiator") int initiatorId, @Bind("target") int targetId);

    /**
     * Invalidates an ignore relationship.
     *
     * @param id the ignore ID
     */
    @SqlUpdate("UPDATE `fp_ignore` SET `valid` = false WHERE `id` = :id")
    void invalidate(@Bind("id") int id);

    /**
     * Finds all active ignores by an initiator.
     *
     * @param initiatorId the ID of the player who is ignoring
     * @return list of ignore records
     */
    @SqlQuery("SELECT * FROM `fp_ignore` WHERE `initiator` = :initiator AND `valid` = true")
    List<Ignore> findByInitiator(@Bind("initiator") int initiatorId);

    /**
     * Finds a specific ignore relationship.
     *
     * @param initiatorId the ID of the player who is ignoring
     * @param targetId the ID of the player being ignored
     * @return optional containing the ignore record if found
     */
    @SqlQuery("SELECT * FROM `fp_ignore` WHERE `initiator` = :initiator AND `target` = :target AND `valid` = true")
    Optional<Ignore> findByInitiatorAndTarget(@Bind("initiator") int initiatorId, @Bind("target") int targetId);

}