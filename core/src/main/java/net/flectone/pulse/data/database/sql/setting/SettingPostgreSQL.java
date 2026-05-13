package net.flectone.pulse.data.database.sql.setting;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

/**
 * PostgreSQL-specific implementation of {@link SettingSQL}.
 *
 * @author TheFaser
 * @since 1.10.0
 */
public interface SettingPostgreSQL extends SettingSQL {

    @Override
    @SqlUpdate("INSERT INTO `fp_setting` (`player`, `type`, `value`) VALUES (:player, :type, :value) ON CONFLICT (`player`, `type`) DO UPDATE SET `value` = :value")
    void upsert(@Bind("player") int playerId, @Bind("type") String type, @Bind("value") String value);

}