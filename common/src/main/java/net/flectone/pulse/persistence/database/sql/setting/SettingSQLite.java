package net.flectone.pulse.persistence.database.sql.setting;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface SettingSQLite extends SettingSQL {

    @Override
    @SqlUpdate("INSERT OR REPLACE INTO `fp_setting` (`player`, `type`, `value`) VALUES (:player, :type, :value)")
    void upsert(@Bind("player") int playerId, @Bind("type") String type, @Bind("value") String value);

}