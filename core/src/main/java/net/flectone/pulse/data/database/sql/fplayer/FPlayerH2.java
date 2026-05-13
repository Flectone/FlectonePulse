package net.flectone.pulse.data.database.sql.fplayer;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

/**
 * H2-specific implementation of {@link FPlayerSQL}.
 *
 * @author TheFaser
 * @since 1.10.0
 */
public interface FPlayerH2 extends FPlayerSQL {

    @Override
    @SqlUpdate("INSERT IGNORE INTO `fp_player` (`id`, `uuid`, `name`) VALUES (:id, :uuid, :name)")
    void insertOrIgnore(@Bind("id") int id, @Bind("uuid") String uuid, @Bind("name") String name);

}