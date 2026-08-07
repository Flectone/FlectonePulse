package net.flectone.pulse.persistence.database.sql.fplayer;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface FPlayerMySQL extends FPlayerSQL {

    @Override
    @SqlUpdate(
            """
            UPDATE `fp_player` p
            LEFT JOIN `fp_setting` s ON s.`player` = p.`id` AND s.`type` = 'server'
            SET p.`online` = false
            WHERE s.`value` IS NULL OR s.`value` = :server
            """
    )
    void setOfflineByServer(@Bind("server") String server);

    @Override
    @SqlUpdate("INSERT IGNORE INTO `fp_player` (`id`, `uuid`, `name`) VALUES (:id, :uuid, :name)")
    void insertOrIgnore(@Bind("id") int id, @Bind("uuid") String uuid, @Bind("name") String name);

}