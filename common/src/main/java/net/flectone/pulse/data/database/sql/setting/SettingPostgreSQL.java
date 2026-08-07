package net.flectone.pulse.data.database.sql.setting;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface SettingPostgreSQL extends SettingSQL {

    @Override
    @SqlUpdate("INSERT INTO `fp_setting` (`player`, `type`, `value`) VALUES (:player, :type, :value) ON CONFLICT (`player`, `type`) DO UPDATE SET `value` = :value")
    void upsert(@Bind("player") int playerId, @Bind("type") String type, @Bind("value") String value);

}