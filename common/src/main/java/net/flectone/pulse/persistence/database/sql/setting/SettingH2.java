package net.flectone.pulse.persistence.database.sql.setting;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface SettingH2 extends SettingSQL {

    @Override
    @SqlUpdate("MERGE INTO `fp_setting` (`player`, `type`, `value`) KEY(`player`, `type`) VALUES (:player, :type, :value)")
    void upsert(@Bind("player") int playerId, @Bind("type") String type, @Bind("value") String value);

}