package net.flectone.pulse.database.sql;

import net.flectone.pulse.module.command.ignore.model.Ignore;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

public interface IgnoreSQL extends SQL {

    @SqlUpdate("INSERT INTO `ignore` (`date`, `initiator`, `target`) VALUES (:date, :initiator, :target)")
    @GetGeneratedKeys("id")
    int insert(@Bind("date") long date, @Bind("initiator") int initiatorId, @Bind("target") int targetId);

    @SqlUpdate("DELETE FROM `ignore` WHERE `id` = :id")
    void delete(@Bind("id") int id);

    @SqlQuery("SELECT * FROM `ignore` WHERE `initiator` = :initiator")
    List<Ignore> findByInitiator(@Bind("initiator") int initiatorId);

}
