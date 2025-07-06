package net.flectone.pulse.database.sql;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Optional;

public interface SettingSQL extends SQL {

    @SqlQuery("SELECT `value` FROM `setting` WHERE `player` = :player AND `type` = :type")
    Optional<String> getSetting(@Bind("player") int playerId, @Bind("type") String type);

    @SqlUpdate("DELETE FROM `setting` WHERE `player` = :player AND `type` = :type")
    void deleteSetting(@Bind("player") int playerId, @Bind("type") String type);

    @SqlUpdate("INSERT INTO `setting` (`player`, `type`, `value`) VALUES (:player, :type, :value)")
    void insertSetting(@Bind("player") int playerId, @Bind("type") String type, @Bind("value") String value);

    @SqlUpdate("UPDATE `setting` SET `value` = :value WHERE `player` = :player AND `type` = :type")
    int updateSetting(@Bind("player") int playerId, @Bind("type") String type, @Bind("value") String value);

}

