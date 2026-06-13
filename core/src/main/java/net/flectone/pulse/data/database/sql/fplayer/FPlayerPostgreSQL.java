package net.flectone.pulse.data.database.sql.fplayer;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

/**
 * PostgreSQL-specific implementation of {@link FPlayerSQL}.
 *
 * @author TheFaser
 * @since 1.10.0
 */
public interface FPlayerPostgreSQL extends FPlayerSQL {

    @Override
    @SqlUpdate(
        """
        INSERT INTO `fp_player` (`id`, `uuid`, `name`)
        VALUES (:id, :uuid, :name)
        ON CONFLICT (`id`) DO NOTHING
        """
    )
    void insertOrIgnore(@Bind("id") int id, @Bind("uuid") String uuid, @Bind("name") String name);

}