package net.flectone.pulse.data.database.sql.version;

import net.flectone.pulse.data.database.sql.SQL;
import net.flectone.pulse.exception.UnsupportedDatabaseOperationException;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.util.Optional;

public interface VersionSQL extends SQL {

    @SqlQuery("SELECT `name` FROM `fp_version` WHERE `id` = 1")
    Optional<String> find();

    default void upsert(@Bind("name") String name) {
        throw new UnsupportedDatabaseOperationException();
    }

}