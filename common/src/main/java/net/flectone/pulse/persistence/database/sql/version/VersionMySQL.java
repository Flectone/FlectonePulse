package net.flectone.pulse.persistence.database.sql.version;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface VersionMySQL extends VersionSQL {

    @Override
    @SqlUpdate("INSERT INTO `fp_version` (`id`, `name`) VALUES (1, :name) ON DUPLICATE KEY UPDATE `name` = :name")
    void upsert(@Bind("name") String name);

}