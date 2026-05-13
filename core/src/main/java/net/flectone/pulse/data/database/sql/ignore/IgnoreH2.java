package net.flectone.pulse.data.database.sql.ignore;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

/**
 * H2-specific implementation of {@link IgnoreSQL}.
 *
 * @author TheFaser
 * @since 1.10.0
 */
public interface IgnoreH2 extends IgnoreSQL {

    @Override
    @SqlUpdate("MERGE INTO `fp_ignore` (`date`, `initiator`, `target`, `valid`) KEY(`initiator`, `target`) VALUES (:date, :initiator, :target, true)")
    void upsert(@Bind("date") long date, @Bind("initiator") int initiatorId, @Bind("target") int targetId);

}