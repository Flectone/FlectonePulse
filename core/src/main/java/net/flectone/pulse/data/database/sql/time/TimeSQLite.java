package net.flectone.pulse.data.database.sql.time;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

/**
 * SQLite-specific implementation of {@link TimeSQL}.
 *
 * @author TheFaser
 * @since 1.10.0
 */
public interface TimeSQLite extends TimeSQL {

    @Override
    @SqlUpdate(
        """
        INSERT INTO `fp_time` (`player`, `first`, `last`, `total`, `sessions`) VALUES (:player, :first, :last, :total, :sessions)
        ON CONFLICT(`player`) DO UPDATE SET `last` = :last, `sessions` = `sessions` + :sessions
        """
    )
    void upsert(@Bind("player") int playerId, @Bind("first") long first, @Bind("last") long last, @Bind("total") long total, @Bind("sessions") int sessions);
}