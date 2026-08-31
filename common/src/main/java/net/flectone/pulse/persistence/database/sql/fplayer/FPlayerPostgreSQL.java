package net.flectone.pulse.persistence.database.sql.fplayer;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface FPlayerPostgreSQL extends FPlayerSQL {

    @Override
    @SqlUpdate(
        """
        INSERT INTO `fp_player` (`id`, `uuid`, `name`)
        VALUES (:id, :uuid, :name)
        ON CONFLICT DO NOTHING
        """
    )
    void insertOrIgnore(@Bind("id") int id, @Bind("uuid") String uuid, @Bind("name") String name);

}