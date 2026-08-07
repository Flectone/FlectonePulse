package net.flectone.pulse.persistence.database.sql.setting;

import net.flectone.pulse.exception.UnsupportedDatabaseOperationException;
import net.flectone.pulse.persistence.database.sql.SQL;
import org.jdbi.v3.sqlobject.config.KeyColumn;
import org.jdbi.v3.sqlobject.config.ValueColumn;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.util.Map;

public interface SettingSQL extends SQL {

    @KeyColumn("type")
    @ValueColumn("value")
    @SqlQuery("SELECT `type`, `value` FROM `fp_setting` WHERE `player` = :player")
    Map<String, String> findByPlayer(@Bind("player") int playerId);

    default void upsert(@Bind("player") int playerId, @Bind("type") String type, @Bind("value") String value) {
        throw new UnsupportedDatabaseOperationException();
    }

}