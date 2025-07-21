package net.flectone.pulse.database.sql;

import net.flectone.pulse.database.dao.ColorsDAO;
import org.jdbi.v3.sqlobject.config.KeyColumn;
import org.jdbi.v3.sqlobject.config.ValueColumn;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ColorsSQL extends SQL {

    @SqlUpdate("DELETE FROM `player_color` WHERE `player` = :playerId")
    void deleteAllColors(@Bind("playerId") int playerId);

    @SqlQuery("SELECT `number`, `color` FROM `player_color` WHERE `player` = :playerId")
    @KeyColumn("number")
    @ValueColumn("color")
    Map<Integer, Integer> getCurrentColors(@Bind("playerId") int playerId);

    @SqlQuery("SELECT `id` FROM `color` WHERE `name` = :colorName")
    Optional<Integer> findColorIdByName(@Bind("colorName") String colorName);

    @SqlUpdate("INSERT INTO `color` (`name`) VALUES (:colorName)")
    @GetGeneratedKeys("id")
    int insertColor(@Bind("colorName") String colorName);

    @SqlUpdate("UPDATE `player_color` SET `color` = :colorId WHERE `player` = :playerId AND `number` = :number")
    void updatePlayerColor(@Bind("playerId") int playerId, @Bind("number") int number, @Bind("colorId") int colorId);

    @SqlUpdate("INSERT INTO `player_color` (`number`, `player`, `color`) VALUES (:number, :playerId, :colorId)")
    void insertPlayerColor(@Bind("playerId") int playerId, @Bind("number") int number, @Bind("colorId") int colorId);

    @SqlUpdate("DELETE FROM `player_color` WHERE `player` = :playerId AND `number` IN (<numbers>)")
    void deletePlayerColors(@Bind("playerId") int playerId, @BindList("numbers") List<Integer> numbers);

    @SqlQuery("SELECT `number`, `color`.`name` FROM `player_color` LEFT JOIN `color` ON `player_color`.`color` = `color`.`id` WHERE `player_color`.`player` = :playerId")
    List<ColorsDAO.ColorEntry> loadPlayerColors(@Bind("playerId") int playerId);

}
