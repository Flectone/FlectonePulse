package net.flectone.pulse.data.database.sql;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Optional;

public interface VersionSQL extends SQL {

    @SqlQuery("SELECT `name` FROM `version` WHERE `id` = 1")
    Optional<String> find();

    @SqlUpdate("INSERT INTO `version` (`name`) VALUES (:name)")
    void insert(@Bind("name") String name);

    @SqlUpdate("UPDATE `version` SET `name` = :name WHERE `id` = 1")
    void update(@Bind("name") String name);

}
