package net.flectone.pulse.persistence.database.sql.fcolor;

import net.flectone.pulse.exception.UnsupportedDatabaseOperationException;
import net.flectone.pulse.persistence.database.dao.FColorDao;
import net.flectone.pulse.persistence.database.sql.SQL;
import org.jdbi.v3.sqlobject.config.KeyColumn;
import org.jdbi.v3.sqlobject.config.ValueColumn;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Map;

public interface FColorSQL extends SQL {

    @SqlQuery("SELECT `number`, `fp_fcolor`.`name`, `type` FROM `fp_player_fcolor` LEFT JOIN `fp_fcolor` ON `fp_player_fcolor`.`fcolor` = `fp_fcolor`.`id` WHERE `fp_player_fcolor`.`player` = :playerId")
    List<FColorDao.FColorInfo> findFColors(@Bind("playerId") int playerId);

    @KeyColumn("name")
    @ValueColumn("id")
    @SqlQuery("SELECT `name`, `id` FROM `fp_fcolor` WHERE `name` IN (<names>)")
    Map<String, Integer> findFColorIdsByNames(@BindList("names") List<String> names);

    @SqlUpdate("DELETE FROM `fp_player_fcolor` WHERE `player` = :playerId")
    void deleteFColors(@Bind("playerId") int playerId);

    @SqlUpdate("DELETE FROM `fp_player_fcolor` WHERE `player` = :playerId AND `type` = :type")
    void deleteFColors(@Bind("playerId") int playerId, @Bind("type") String type);

    @SqlUpdate("DELETE FROM `fp_player_fcolor` WHERE `player` = :playerId AND `type` = :type AND `number` IN (<numbers>)")
    void deleteFColors(@Bind("playerId") int playerId, @Bind("type") String type, @BindList("numbers") List<Integer> numbers);

    default void insertFColorsIfAbsent(@Bind("name") List<String> names) {
        throw new UnsupportedDatabaseOperationException();
    }

    default void batchUpsertPlayerFColors(@Bind("playerId") int playerId, @Bind("number") List<Integer> numbers, @Bind("fcolorId") List<Integer> fcolorIds, @Bind("type") String type) {
        throw new UnsupportedDatabaseOperationException();
    }

}