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

    @SqlQuery("SELECT `number`, `fp_fcolor`.`name`, `type` FROM `fp_player_fcolor` LEFT JOIN `fp_fcolor` ON `fp_player_fcolor`.`fcolor` = `fp_fcolor`.`id` WHERE `fp_player_fcolor`.`player` = :playerId AND `type` = :type")
    Set<FColor> findFColors(@Bind("playerId") int playerId, @Bind("type") String type);

    @SqlUpdate("INSERT INTO `fp_player_fcolor` (`number`, `player`, `fcolor`, `type`) VALUES (:number, :playerId, :fcolorId, :type)")
    void insertFColor(@Bind("playerId") int playerId, @Bind("number") int number, @Bind("fcolorId") int fcolorId, @Bind("type") String type);

    @SqlUpdate("UPDATE `fp_player_fcolor` SET `fcolor` = :fcolorId WHERE `player` = :playerId AND `number` = :number AND `type` = :type")
    void updateFColor(@Bind("playerId") int playerId, @Bind("number") int number, @Bind("fcolorId") int fcolorId, @Bind("type") String type);

    @SqlUpdate("DELETE FROM `fp_player_fcolor` WHERE `player` = :playerId")
    void deleteFColors(@Bind("playerId") int playerId);

    @SqlUpdate("DELETE FROM `fp_player_fcolor` WHERE `player` = :playerId AND `type` = :type")
    void deleteFColors(@Bind("playerId") int playerId, @Bind("type") String type);

    @SqlUpdate("DELETE FROM `fp_player_fcolor` WHERE `player` = :playerId AND `type` = :type AND `number` IN (<numbers>)")
    void deleteFColors(@Bind("playerId") int playerId, @Bind("type") String type, @BindList("numbers") List<Integer> numbers);

    @SqlQuery("SELECT `id` FROM `fp_fcolor` WHERE `name` = :fcolorName")
    Optional<Integer> findFColorIdByName(@Bind("fcolorName") String fcolorName);

    @SqlUpdate("INSERT INTO `fp_fcolor` (`name`) VALUES (:fcolorName)")
    @GetGeneratedKeys("id")
    int insertFColor(@Bind("fcolorName") String fcolorName);

}
