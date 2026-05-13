package net.flectone.pulse.data.database.sql.version;

import net.flectone.pulse.data.database.sql.SQL;
import net.flectone.pulse.exception.UnsupportedDatabaseOperationException;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.util.Optional;

/**
 * SQL interface for version Database in FlectonePulse.
 *
 * @author TheFaser
 * @since 1.6.0
 */
public interface VersionSQL extends SQL {

    /**
     * Finds the stored version name.
     *
     * @return optional containing the version name if found
     */
    @SqlQuery("SELECT `name` FROM `fp_version` WHERE `id` = 1")
    Optional<String> find();

    /**
     * Inserts or updates the version name.
     *
     * @param name the version name to persist
     * @throws UnsupportedDatabaseOperationException if not overridden
     */
    default void upsert(@Bind("name") String name) {
        throw new UnsupportedDatabaseOperationException();
    }

}