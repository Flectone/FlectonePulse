package net.flectone.pulse.data.database.sql.ignore;

import net.flectone.pulse.data.database.sql.SQL;
import net.flectone.pulse.exception.UnsupportedDatabaseOperationException;
import net.flectone.pulse.module.command.ignore.model.Ignore;
import org.jdbi.v3.sqlobject.customizer.Bind;
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

    /**
     * Inserts a new ignore or reactivates an existing one.
     *
     * @param date the timestamp
     * @param initiatorId the ID of the player who is ignoring
     * @param targetId the ID of the player being ignored
     * @throws UnsupportedDatabaseOperationException if not overridden
     */
    default void upsert(@Bind("date") long date, @Bind("initiator") int initiatorId, @Bind("target") int targetId) {
        throw new UnsupportedDatabaseOperationException();
    }

}