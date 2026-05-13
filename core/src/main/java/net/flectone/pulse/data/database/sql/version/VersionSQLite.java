package net.flectone.pulse.data.database.sql.version;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

/**
 * SQLite-specific implementation of {@link VersionSQL}.
 *
 * @author TheFaser
 * @since 1.10.0
 */
public interface VersionSQLite extends VersionSQL {

    @Override
    @SqlUpdate("INSERT OR REPLACE INTO `fp_version` (`id`, `name`) VALUES (1, :name)")
    void upsert(@Bind("name") String name);

}