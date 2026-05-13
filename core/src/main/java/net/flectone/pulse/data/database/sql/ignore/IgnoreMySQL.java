package net.flectone.pulse.data.database.sql.ignore;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

/**
 * MySQL-specific implementation of {@link IgnoreSQL}.
 *
 * @author TheFaser
 * @since 1.10.0
 */
public interface IgnoreMySQL extends IgnoreSQL {

    @Override
    @SqlUpdate("INSERT INTO `fp_ignore` (`date`, `initiator`, `target`) VALUES (:date, :initiator, :target) ON DUPLICATE KEY UPDATE `date` = :date, `valid` = true")
    void upsert(@Bind("date") long date, @Bind("initiator") int initiatorId, @Bind("target") int targetId);

}