package net.flectone.pulse.data.database.sql;

import org.jdbi.v3.sqlobject.config.KeyColumn;
import org.jdbi.v3.sqlobject.config.ValueColumn;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Map;

public interface SettingSQL extends SQL {

    @KeyColumn("type")
    @ValueColumn("value")
    @SqlQuery("SELECT `type`, `value` FROM `setting` WHERE `player` = :player")
    Map<String, String> findByPlayer(@Bind("player") int playerId);

    @SqlUpdate("INSERT INTO `setting` (`player`, `type`, `value`) VALUES (:player, :type, :value)")
    void insert(@Bind("player") int playerId, @Bind("type") String type, @Bind("value") String value);

    @SqlUpdate("UPDATE `setting` SET `value` = :value WHERE `player` = :player AND `type` = :type")
    int update(@Bind("player") int playerId, @Bind("type") String type, @Bind("value") String value);

}

