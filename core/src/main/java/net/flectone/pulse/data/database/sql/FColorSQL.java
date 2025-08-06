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

public interface FColorSQL extends SQL {

    @SqlQuery("SELECT `number`, `fcolor`.`name`, `type` FROM `player_fcolor` LEFT JOIN `fcolor` ON `player_fcolor`.`fcolor` = `fcolor`.`id` WHERE `player_fcolor`.`player` = :playerId AND `type` = :type")
    Set<FColor> findFColors(@Bind("playerId") int playerId, @Bind("type") String type);

    @SqlUpdate("INSERT INTO `player_fcolor` (`number`, `player`, `fcolor`, `type`) VALUES (:number, :playerId, :fcolorId, :type)")
    void insertFColor(@Bind("playerId") int playerId, @Bind("number") int number, @Bind("fcolorId") int fcolorId, @Bind("type") String type);

    @SqlUpdate("UPDATE `player_fcolor` SET `fcolor` = :fcolorId WHERE `player` = :playerId AND `number` = :number AND `type` = :type")
    void updateFColor(@Bind("playerId") int playerId, @Bind("number") int number, @Bind("fcolorId") int fcolorId, @Bind("type") String type);

    @SqlUpdate("DELETE FROM `player_fcolor` WHERE `player` = :playerId")
    void deleteFColors(@Bind("playerId") int playerId);

    @SqlUpdate("DELETE FROM `player_fcolor` WHERE `player` = :playerId AND `type` = :type")
    void deleteFColors(@Bind("playerId") int playerId, @Bind("type") String type);

    @SqlUpdate("DELETE FROM `player_fcolor` WHERE `player` = :playerId AND `type` = :type AND `number` IN (<numbers>)")
    void deleteFColors(@Bind("playerId") int playerId, @Bind("type") String type, @BindList("numbers") List<Integer> numbers);

    @SqlQuery("SELECT `id` FROM `fcolor` WHERE `name` = :fcolorName")
    Optional<Integer> findFColorIdByName(@Bind("fcolorName") String fcolorName);

    @SqlUpdate("INSERT INTO `fcolor` (`name`) VALUES (:fcolorName)")
    @GetGeneratedKeys("id")
    int insertFColor(@Bind("fcolorName") String fcolorName);

}
