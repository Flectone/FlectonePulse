package net.flectone.pulse.data.database.sql.version;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface VersionH2 extends VersionSQL {

    @Override
    @SqlUpdate("MERGE INTO `fp_version` (`id`, `name`) KEY(`id`) VALUES (1, :name)")
    void upsert(@Bind("name") String name);

}