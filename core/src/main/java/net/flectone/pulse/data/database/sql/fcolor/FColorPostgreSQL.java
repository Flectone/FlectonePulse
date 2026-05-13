package net.flectone.pulse.data.database.sql.fcolor;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlBatch;

import java.util.List;

/**
 * PostgreSQL-specific implementation of {@link FColorSQL}.
 *
 * @author TheFaser
 * @since 1.10.0
 */
public interface FColorPostgreSQL extends FColorSQL {

    @Override
    @SqlBatch("INSERT INTO `fp_fcolor` (`name`) VALUES (:name) ON CONFLICT (`name`) DO NOTHING")
    void insertFColorsIfAbsent(@Bind("name") List<String> names);

    @Override
    @SqlBatch(
        """
        INSERT INTO `fp_player_fcolor` (`player`, `number`, `fcolor`, `type`)
        VALUES (:playerId, :number, :fcolorId, :type)
        ON CONFLICT (`player`, `number`, `type`) DO UPDATE SET `fcolor` = EXCLUDED.`fcolor`
        """
    )
    void batchUpsertPlayerFColors(@Bind("playerId") int playerId, @Bind("number") List<Integer> numbers, @Bind("fcolorId") List<Integer> fcolorIds, @Bind("type") String type);

}