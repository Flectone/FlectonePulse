package net.flectone.pulse.database.sql;

import net.flectone.pulse.database.dao.FPlayerDAO;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;

public interface FPlayerSQL extends SQL {

    @SqlQuery("SELECT * FROM `player` WHERE UPPER(`name`) = UPPER(:name)")
    Optional<FPlayerDAO.PlayerInfo> findByName(@Bind("name") String name);

    @SqlQuery("SELECT * FROM `player` WHERE `uuid` = :uuid")
    Optional<FPlayerDAO.PlayerInfo> findByUUID(@Bind("uuid") String name);

    @SqlUpdate("INSERT INTO `player` (`uuid`, `name`) VALUES (:uuid, :name)")
    void insert(@Bind("uuid") String uuid, @Bind("name") String name);

    @SqlUpdate("INSERT IGNORE INTO `player` (`id`, `uuid`, `name`) VALUES (:id, :uuid, :name)")
    void insertOrIgnoreMySQL(@Bind("id") int id, @Bind("uuid") String uuid, @Bind("name") String name);

    @SqlUpdate("INSERT OR IGNORE INTO `player` (`id`, `uuid`, `name`) VALUES (:id, :uuid, :name)")
    void insertOrIgnoreSQLite(@Bind("id") int id, @Bind("uuid") String uuid, @Bind("name") String name);

    @SqlUpdate("UPDATE `player` SET `online` = :online, `uuid` = :uuid, `name` = :name, `ip` = :ip WHERE `id` = :id")
    void update(@Bind("id") int id, @Bind("online") boolean online, @Bind("uuid") String uuid, @Bind("name") String name, @Bind("ip") String ip);

    @SqlUpdate("UPDATE `player` SET `online` = 0")
    void updateAllToOffline();

    @SqlQuery("SELECT * FROM `player` WHERE `online` = 1")
    List<FPlayerDAO.PlayerInfo> getOnlinePlayers();

    @SqlQuery("SELECT * FROM `player`")
    List<FPlayerDAO.PlayerInfo> getAllPlayers();

    @SqlQuery("SELECT * FROM `player` WHERE UPPER(`name`) = UPPER(:name)")
    Optional<FPlayerDAO.PlayerInfo> getPlayerByName(@Bind("name") String name);

    @SqlQuery("SELECT * FROM `player` WHERE `ip` = :ip")
    Optional<FPlayerDAO.PlayerInfo> getPlayerByIp(@Bind("ip") String ip);

    @SqlQuery("SELECT * FROM `player` WHERE `uuid` = :uuid")
    Optional<FPlayerDAO.PlayerInfo> getPlayerByUuid(@Bind("uuid") String uuid);

    @SqlQuery("SELECT * FROM `player` WHERE `id` = :id")
    Optional<FPlayerDAO.PlayerInfo> getPlayerById(@Bind("id") int id);

}
