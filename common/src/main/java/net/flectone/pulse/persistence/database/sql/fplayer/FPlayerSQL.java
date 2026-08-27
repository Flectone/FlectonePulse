package net.flectone.pulse.persistence.database.sql.fplayer;

import net.flectone.pulse.exception.UnsupportedDatabaseOperationException;
import net.flectone.pulse.persistence.database.dao.FPlayerDAO;
import net.flectone.pulse.persistence.database.sql.SQL;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;

public interface FPlayerSQL extends SQL {

    @SqlQuery("SELECT * FROM `fp_player` WHERE UPPER(`name`) = UPPER(:name) LIMIT 1")
    Optional<FPlayerDAO.PlayerInfo> findByName(@Bind("name") String name);

    @SqlQuery("SELECT * FROM `fp_player` WHERE `uuid` = :uuid")
    Optional<FPlayerDAO.PlayerInfo> findByUUID(@Bind("uuid") String uuid);

    @SqlQuery("SELECT * FROM `fp_player` WHERE `ip` = :ip LIMIT 1")
    Optional<FPlayerDAO.PlayerInfo> findByIp(@Bind("ip") String ip);

    @SqlQuery("SELECT * FROM `fp_player` p WHERE p.`id` = :id")
    Optional<FPlayerDAO.PlayerInfo> findById(@Bind("id") int id);

    @GetGeneratedKeys("id")
    @SqlUpdate("INSERT INTO `fp_player` (`online`, `uuid`, `name`, `ip`) VALUES (:online, :uuid, :name, :ip)")
    int insert(@Bind("online") boolean online, @Bind("uuid") String uuid, @Bind("name") String name, @Bind("ip") String ip);

    @SqlUpdate("UPDATE `fp_player` SET `online` = :online, `uuid` = :uuid, `name` = :name, `ip` = :ip WHERE `id` = :id")
    void update(@Bind("id") int id, @Bind("online") boolean online, @Bind("uuid") String uuid, @Bind("name") String name, @Bind("ip") String ip);

    @SqlUpdate(
            """
            UPDATE `fp_player` SET `online` = false
            WHERE `online` = true AND `id` IN (
                SELECT p.`id` FROM `fp_player` p
                LEFT JOIN `fp_setting` s ON s.`player` = p.`id` AND s.`type` = 'server'
                WHERE s.`value` IS NULL OR s.`value` = :server
            )
            """
    )
    void setOfflineByServer(@Bind("server") String server);

    @SqlQuery("SELECT * FROM `fp_player` WHERE `online` = true")
    List<FPlayerDAO.PlayerInfo> getOnlinePlayers();

    @SqlQuery("SELECT COUNT(id) FROM `fp_player` WHERE `ip` = :ip")
    int getTotalPlayersCountByIp(@Bind("ip") String ip);

    @SqlQuery("SELECT * FROM `fp_player` WHERE `ip` = :ip ORDER BY `id` DESC LIMIT :limit OFFSET :offset")
    List<FPlayerDAO.PlayerInfo> getPlayersByIp(@Bind("ip") String ip, @Bind("limit") int limit, @Bind("offset") int offset);

    @SqlQuery("SELECT * FROM `fp_player`")
    List<FPlayerDAO.PlayerInfo> getAllPlayers();

    default void insertOrIgnore(@Bind("id") int id, @Bind("uuid") String uuid, @Bind("name") String name) {
        throw new UnsupportedDatabaseOperationException();
    }

}