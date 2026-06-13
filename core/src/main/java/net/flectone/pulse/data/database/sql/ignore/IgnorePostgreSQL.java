package net.flectone.pulse.data.database.sql.ignore;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

/**
 * PostgreSQL-specific implementation of {@link IgnoreSQL}.
 *
 * @author TheFaser
 * @since 1.10.0
 */
public interface IgnorePostgreSQL extends IgnoreSQL {

    @Override
    @SqlUpdate("INSERT INTO `fp_ignore` (`date`, `initiator`, `target`) VALUES (:date, :initiator, :target) ON CONFLICT (`initiator`, `target`) DO UPDATE SET `date` = :date, `valid` = true")
    void upsert(@Bind("date") long date, @Bind("initiator") int initiatorId, @Bind("target") int targetId);

}