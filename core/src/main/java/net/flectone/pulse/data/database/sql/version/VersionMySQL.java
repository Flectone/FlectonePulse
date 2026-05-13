package net.flectone.pulse.data.database.sql.version;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

/**
 * MySQL-specific implementation of {@link VersionSQL}.
 *
 * @author TheFaser
 * @since 1.10.0
 */
public interface VersionMySQL extends VersionSQL {

    @Override
    @SqlUpdate("INSERT INTO `fp_version` (`id`, `name`) VALUES (1, :name) ON DUPLICATE KEY UPDATE `name` = :name")
    void upsert(@Bind("name") String name);

}