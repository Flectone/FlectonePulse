package net.flectone.pulse.data.database.sql;

import net.flectone.pulse.model.FColor;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * SQL interface for player color data operations in FlectonePulse.
 * Defines database queries for managing player color configurations.
 *
 * @author TheFaser
 * @since 0.9.0
 */
public interface FColorSQL extends SQL {

    /**
     * Finds colors for a player by type.
     *
     * @param playerId the player ID
     * @param type the color type
     * @return set of colors for the player
     */
    @SqlQuery("SELECT `number`, `fp_fcolor`.`name`, `type` FROM `fp_player_fcolor` LEFT JOIN `fp_fcolor` ON `fp_player_fcolor`.`fcolor` = `fp_fcolor`.`id` WHERE `fp_player_fcolor`.`player` = :playerId AND `type` = :type")
    Set<FColor> findFColors(@Bind("playerId") int playerId, @Bind("type") String type);

    /**
     * Inserts a color for a player.
     *
     * @param playerId the player ID
     * @param number the color number
     * @param fcolorId the color ID
     * @param type the color type
     */
    @SqlUpdate("INSERT INTO `fp_player_fcolor` (`number`, `player`, `fcolor`, `type`) VALUES (:number, :playerId, :fcolorId, :type)")
    void insertFColor(@Bind("playerId") int playerId, @Bind("number") int number, @Bind("fcolorId") int fcolorId, @Bind("type") String type);

    /**
     * Updates a color for a player.
     *
     * @param playerId the player ID
     * @param number the color number
     * @param fcolorId the color ID
     * @param type the color type
     */
    @SqlUpdate("UPDATE `fp_player_fcolor` SET `fcolor` = :fcolorId WHERE `player` = :playerId AND `number` = :number AND `type` = :type")
    void updateFColor(@Bind("playerId") int playerId, @Bind("number") int number, @Bind("fcolorId") int fcolorId, @Bind("type") String type);

    /**
     * Deletes all colors for a player.
     *
     * @param playerId the player ID
     */
    @SqlUpdate("DELETE FROM `fp_player_fcolor` WHERE `player` = :playerId")
    void deleteFColors(@Bind("playerId") int playerId);

    /**
     * Deletes colors for a player by type.
     *
     * @param playerId the player ID
     * @param type the color type
     */
    @SqlUpdate("DELETE FROM `fp_player_fcolor` WHERE `player` = :playerId AND `type` = :type")
    void deleteFColors(@Bind("playerId") int playerId, @Bind("type") String type);

    /**
     * Deletes specific colors for a player by type and numbers.
     *
     * @param playerId the player ID
     * @param type the color type
     * @param numbers the color numbers to delete
     */
    @SqlUpdate("DELETE FROM `fp_player_fcolor` WHERE `player` = :playerId AND `type` = :type AND `number` IN (<numbers>)")
    void deleteFColors(@Bind("playerId") int playerId, @Bind("type") String type, @BindList("numbers") List<Integer> numbers);

    /**
     * Finds a color ID by name.
     *
     * @param fcolorName the color name
     * @return optional containing the color ID if found
     */
    @SqlQuery("SELECT `id` FROM `fp_fcolor` WHERE `name` = :fcolorName")
    Optional<Integer> findFColorIdByName(@Bind("fcolorName") String fcolorName);

    /**
     * Inserts a new color name and returns its ID.
     *
     * @param fcolorName the color name
     * @return the generated color ID
     */
    @SqlUpdate("INSERT INTO `fp_fcolor` (`name`) VALUES (:fcolorName)")
    @GetGeneratedKeys("id")
    int insertFColor(@Bind("fcolorName") String fcolorName);

}