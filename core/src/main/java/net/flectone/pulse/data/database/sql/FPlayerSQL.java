package net.flectone.pulse.data.database.sql;

import net.flectone.pulse.data.database.dao.FPlayerDAO;
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

    @SqlQuery("SELECT * FROM `player` WHERE `ip` = :ip LIMIT 1")
    Optional<FPlayerDAO.PlayerInfo> findByIp(@Bind("ip") String ip);

    @SqlQuery("SELECT * FROM `player` WHERE `id` = :id")
    Optional<FPlayerDAO.PlayerInfo> findById(@Bind("id") int id);

    @SqlUpdate("INSERT INTO `player` (`uuid`, `name`) VALUES (:uuid, :name)")
    void insert(@Bind("uuid") String uuid, @Bind("name") String name);

    @SqlUpdate("INSERT INTO `player` (`id`, `uuid`, `name`) VALUES (:id, :uuid, :name)")
    void insertWithId(@Bind("id") int id, @Bind("uuid") String uuid, @Bind("name") String name);

    @SqlUpdate("UPDATE `player` SET `online` = :online, `uuid` = :uuid, `name` = :name, `ip` = :ip WHERE `id` = :id")
    void update(@Bind("id") int id, @Bind("online") boolean online, @Bind("uuid") String uuid, @Bind("name") String name, @Bind("ip") String ip);

    @SqlQuery("SELECT * FROM `player` WHERE `online` = true")
    List<FPlayerDAO.PlayerInfo> getOnlinePlayers();

    @SqlQuery("SELECT * FROM `player`")
    List<FPlayerDAO.PlayerInfo> getAllPlayers();

}
