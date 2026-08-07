package net.flectone.pulse.persistence.database.sql.version;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface VersionSQLite extends VersionSQL {

    @Override
    @SqlUpdate("INSERT OR REPLACE INTO `fp_version` (`id`, `name`) VALUES (1, :name)")
    void upsert(@Bind("name") String name);

}