package net.flectone.pulse.persistence.database.sql.ignore;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface IgnoreH2 extends IgnoreSQL {

    @Override
    @SqlUpdate("MERGE INTO `fp_ignore` (`date`, `initiator`, `target`, `valid`) KEY(`initiator`, `target`) VALUES (:date, :initiator, :target, true)")
    void upsert(@Bind("date") long date, @Bind("initiator") int initiatorId, @Bind("target") int targetId);

}