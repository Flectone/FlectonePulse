package net.flectone.pulse.data.database.sql;

import net.flectone.pulse.data.database.dao.FPlayerDAO;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;

/**
 * SQL interface for player data operations in FlectonePulse.
 * Defines database queries for player management and retrieval.
 *
 * @author TheFaser
 * @since 0.9.0
 */
public interface FPlayerSQL extends SQL {

    /**
     * Finds a player by name (case-insensitive).
     *
     * @param name the player name
     * @return optional containing player info if found
     */
    @SqlQuery("SELECT * FROM `fp_player` WHERE UPPER(`name`) = UPPER(:name)")
    Optional<FPlayerDAO.PlayerInfo> findByName(@Bind("name") String name);

    /**
     * Finds a player by UUID.
     *
     * @param uuid the player UUID
     * @return optional containing player info if found
     */
    @SqlQuery("SELECT * FROM `fp_player` WHERE `uuid` = :uuid")
    Optional<FPlayerDAO.PlayerInfo> findByUUID(@Bind("uuid") String uuid);

    /**
     * Finds a player by IP address.
     *
     * @param ip the IP address
     * @return optional containing player info if found
     */
    @SqlQuery("SELECT * FROM `fp_player` WHERE `ip` = :ip LIMIT 1")
    Optional<FPlayerDAO.PlayerInfo> findByIp(@Bind("ip") String ip);

    /**
     * Finds a player by database ID.
     *
     * @param id the player database ID
     * @return optional containing player info if found
     */
    @SqlQuery("SELECT * FROM `fp_player` WHERE `id` = :id")
    Optional<FPlayerDAO.PlayerInfo> findById(@Bind("id") int id);

    /**
     * Inserts a new player.
     *
     * @param uuid the player UUID
     * @param name the player name
     */
    @SqlUpdate("INSERT INTO `fp_player` (`uuid`, `name`) VALUES (:uuid, :name)")
    void insert(@Bind("uuid") String uuid, @Bind("name") String name);

    /**
     * Inserts a new player with a specific ID.
     *
     * @param id the player database ID
     * @param uuid the player UUID
     * @param name the player name
     */
    @SqlUpdate("INSERT INTO `fp_player` (`id`, `uuid`, `name`) VALUES (:id, :uuid, :name)")
    void insertWithId(@Bind("id") int id, @Bind("uuid") String uuid, @Bind("name") String name);

    /**
     * Updates player information.
     *
     * @param id the player database ID
     * @param online whether the player is online
     * @param uuid the player UUID
     * @param name the player name
     * @param ip the player IP address
     */
    @SqlUpdate("UPDATE `fp_player` SET `online` = :online, `uuid` = :uuid, `name` = :name, `ip` = :ip WHERE `id` = :id")
    void update(@Bind("id") int id, @Bind("online") boolean online, @Bind("uuid") String uuid, @Bind("name") String name, @Bind("ip") String ip);

    /**
     * Gets all online players.
     *
     * @return list of online player info
     */
    @SqlQuery("SELECT * FROM `fp_player` WHERE `online` = true")
    List<FPlayerDAO.PlayerInfo> getOnlinePlayers();

    /**
     * Gets all players.
     *
     * @return list of all player info
     */
    @SqlQuery("SELECT * FROM `fp_player`")
    List<FPlayerDAO.PlayerInfo> getAllPlayers();

}