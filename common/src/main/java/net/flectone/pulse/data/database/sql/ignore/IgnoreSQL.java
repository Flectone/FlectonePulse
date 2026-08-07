package net.flectone.pulse.data.database.sql.ignore;

import net.flectone.pulse.data.database.sql.SQL;
import net.flectone.pulse.exception.UnsupportedDatabaseOperationException;
import net.flectone.pulse.module.command.ignore.model.Ignore;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;

public interface IgnoreSQL extends SQL {

    @SqlUpdate("UPDATE `fp_ignore` SET `valid` = false WHERE `id` = :id")
    void invalidate(@Bind("id") int id);

    @SqlQuery("SELECT * FROM `fp_ignore` WHERE `initiator` = :initiator AND `valid` = true")
    List<Ignore> findByInitiator(@Bind("initiator") int initiatorId);

    @SqlQuery("SELECT * FROM `fp_ignore` WHERE `initiator` = :initiator AND `target` = :target AND `valid` = true")
    Optional<Ignore> findByInitiatorAndTarget(@Bind("initiator") int initiatorId, @Bind("target") int targetId);

    default void upsert(@Bind("date") long date, @Bind("initiator") int initiatorId, @Bind("target") int targetId) {
        throw new UnsupportedDatabaseOperationException();
    }

}