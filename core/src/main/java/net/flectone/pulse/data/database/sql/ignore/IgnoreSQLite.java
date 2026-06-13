package net.flectone.pulse.data.database.sql.ignore;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

/**
 * SQLite-specific implementation of {@link IgnoreSQL}.
 *
 * @author TheFaser
 * @since 1.10.0
 */
public interface IgnoreSQLite extends IgnoreSQL {

    @Override
    @SqlUpdate("INSERT OR REPLACE INTO `fp_ignore` (`date`, `initiator`, `target`) VALUES (:date, :initiator, :target)")
    void upsert(@Bind("date") long date, @Bind("initiator") int initiatorId, @Bind("target") int targetId);

}