package net.flectone.pulse.data.database.sql.fcolor;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlBatch;

import java.util.List;

/**
 * H2-specific implementation of {@link FColorSQL}.
 *
 * @author TheFaser
 * @since 1.10.0
 */
public interface FColorH2 extends FColorSQL {

    @Override
    @SqlBatch("MERGE INTO `fp_fcolor` (`name`) KEY(`name`) VALUES (:name)")
    void insertFColorsIfAbsent(@Bind("name") List<String> names);

    @Override
    @SqlBatch(
        """
        MERGE INTO `fp_player_fcolor` (`player`, `number`, `fcolor`, `type`) KEY(`player`, `number`, `type`)
        VALUES (:playerId, :number, :fcolorId, :type)
        """
    )
    void batchUpsertPlayerFColors(@Bind("playerId") int playerId, @Bind("number") List<Integer> numbers, @Bind("fcolorId") List<Integer> fcolorIds, @Bind("type") String type);

}