package net.flectone.pulse.data.database.sql.fplayer;

import net.flectone.pulse.data.database.dao.FPlayerDAO;
import net.flectone.pulse.data.database.reducer.PlayerInfoReducer;
import net.flectone.pulse.data.database.sql.SQL;
import net.flectone.pulse.exception.UnsupportedDatabaseOperationException;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.statement.UseRowReducer;

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
    @SqlQuery("SELECT * FROM `fp_player` WHERE UPPER(`name`) = UPPER(:name) LIMIT 1")
    Optional<FPlayerDAO.PlayerInfo> findByName(@Bind("name") String name);

    /**
     * Finds a player by name (case-insensitive) and load settings.
     *
     * @param name the player name
     * @return optional containing player info if found
     */
    @SqlQuery(
            """
            SELECT p.*, s.`type`, s.`value`
            FROM (SELECT * FROM `fp_player` WHERE UPPER(`name`) = UPPER(:name) LIMIT 1) p
            LEFT JOIN `fp_setting` s ON s.`player` = p.`id`
            """
    )
    @UseRowReducer(PlayerInfoReducer.class)
    Optional<FPlayerDAO.PlayerInfo> findByNameWithSettings(@Bind("name") String name);

    /**
     * Finds a player by UUID.
     *
     * @param uuid the player UUID
     * @return optional containing player info if found
     */
    @SqlQuery("SELECT * FROM `fp_player` WHERE `uuid` = :uuid")
    Optional<FPlayerDAO.PlayerInfo> findByUUID(@Bind("uuid") String uuid);

    /**
     * Finds a player by UUID and load settings.
     *
     * @param uuid the player UUID
     * @return optional containing player info if found
     */
    @SqlQuery(
            """
            SELECT p.*, s.`type`, s.`value`
            FROM `fp_player` p
            LEFT JOIN `fp_setting` s ON s.`player` = p.`id`
            WHERE p.`uuid` = :uuid
            """
    )
    @UseRowReducer(PlayerInfoReducer.class)
    Optional<FPlayerDAO.PlayerInfo> findByUUIDWithSettings(@Bind("uuid") String uuid);

    /**
     * Finds a player by IP address and load settings.
     *
     * @param ip the IP address
     * @return optional containing player info if found
     */
    @SqlQuery(
            """
            SELECT p.*, s.`type`, s.`value`
            FROM (SELECT * FROM `fp_player` WHERE `ip` = :ip LIMIT 1) p
            LEFT JOIN `fp_setting` s ON s.`player` = p.`id`
            """
    )
    @UseRowReducer(PlayerInfoReducer.class)
    Optional<FPlayerDAO.PlayerInfo> findByIpWithSettings(@Bind("ip") String ip);


    /**
     * Finds a player by database ID and load settings.
     *
     * @param id the player database ID
     * @return optional containing player info if found
     */
    @SqlQuery(
            """
            SELECT p.*, s.`type`, s.`value`
            FROM `fp_player` p
            LEFT JOIN `fp_setting` s ON s.`player` = p.`id`
            WHERE p.`id` = :id
            """
    )
    @UseRowReducer(PlayerInfoReducer.class)
    Optional<FPlayerDAO.PlayerInfo> findByIdWithSettings(@Bind("id") int id);

    /**
     * Inserts a new player.
     *
     * @param uuid the player UUID
     * @param name the player name
     * @return the player database ID
     */
    @GetGeneratedKeys("id")
    @SqlUpdate("INSERT INTO `fp_player` (`online`, `uuid`, `name`, `ip`) VALUES (:online, :uuid, :name, :ip)")
    int insert(@Bind("online") boolean online, @Bind("uuid") String uuid, @Bind("name") String name, @Bind("ip") String ip);

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

    /**
     * Inserts a player with the given ID, or does nothing if a conflict occurs.
     *
     * @param id the player database ID
     * @param uuid the player UUID
     * @param name the player name
     * @throws UnsupportedDatabaseOperationException if not overridden
     */
    default void insertOrIgnore(@Bind("id") int id, @Bind("uuid") String uuid, @Bind("name") String name) {
        throw new UnsupportedDatabaseOperationException();
    }

}