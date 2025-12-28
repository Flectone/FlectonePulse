package net.flectone.pulse.data.database.sql;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

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
     * Inserts a new version name.
     *
     * @param name the version name to insert
     */
    @SqlUpdate("INSERT INTO `fp_version` (`id`, `name`) VALUES (1, :name)")
    void insert(@Bind("name") String name);

    /**
     * Updates the version name.
     *
     * @param name the new version name
     */
    @SqlUpdate("UPDATE `fp_version` SET `name` = :name WHERE `id` = 1")
    void update(@Bind("name") String name);

}