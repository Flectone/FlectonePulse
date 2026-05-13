package net.flectone.pulse.data.database.sql.fplayer;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

/**
 * SQLite-specific implementation of {@link FPlayerSQL}.
 *
 * @author TheFaser
 * @since 1.10.0
 */
public interface FPlayerSQLite extends FPlayerSQL {

    @Override
    @SqlUpdate("INSERT OR IGNORE INTO `fp_player` (`id`, `uuid`, `name`) VALUES (:id, :uuid, :name)")
    void insertOrIgnore(@Bind("id") int id, @Bind("uuid") String uuid, @Bind("name") String name);

}