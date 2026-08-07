package net.flectone.pulse.persistence.database.sql.setting;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface SettingMySQL extends SettingSQL {

    @Override
    @SqlUpdate("INSERT INTO `fp_setting` (`player`, `type`, `value`) VALUES (:player, :type, :value) ON DUPLICATE KEY UPDATE `value` = :value")
    void upsert(@Bind("player") int playerId, @Bind("type") String type, @Bind("value") String value);

}