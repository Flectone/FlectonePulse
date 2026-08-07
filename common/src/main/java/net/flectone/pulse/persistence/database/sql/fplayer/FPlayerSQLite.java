package net.flectone.pulse.persistence.database.sql.fplayer;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface FPlayerSQLite extends FPlayerSQL {

    @Override
    @SqlUpdate("INSERT OR IGNORE INTO `fp_player` (`id`, `uuid`, `name`) VALUES (:id, :uuid, :name)")
    void insertOrIgnore(@Bind("id") int id, @Bind("uuid") String uuid, @Bind("name") String name);

}