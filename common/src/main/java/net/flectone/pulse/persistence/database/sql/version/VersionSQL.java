package net.flectone.pulse.persistence.database.sql.version;

import net.flectone.pulse.exception.UnsupportedDatabaseOperationException;
import net.flectone.pulse.persistence.database.sql.SQL;
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