package net.flectone.pulse.persistence.database.sql.time;

import net.flectone.pulse.exception.UnsupportedDatabaseOperationException;
import net.flectone.pulse.model.value.PlayTime;
import net.flectone.pulse.persistence.database.sql.SQL;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;

public interface TimeSQL extends SQL {

    @SqlUpdate("UPDATE `fp_time` SET `last` = :last, `total` = :total WHERE `player` = :player")
    void updateLastSeen(@Bind("last") double last, @Bind("total") double total, @Bind("player") int playerId);

    @SqlQuery("SELECT COUNT(*) FROM `fp_time`")
    int getTotalCount();

    @SqlQuery("SELECT * FROM `fp_time` WHERE `player` = :player")
    Optional<PlayTime> findByPlayer(@Bind("player") int playerId);

    @SqlQuery("SELECT * FROM `fp_time` ORDER BY `total` DESC LIMIT :limit OFFSET :offset")
    List<PlayTime> getAllPlayTimes(@Bind("limit") int limit, @Bind("offset") int offset);

    default void upsert(@Bind("player") int playerId, @Bind("first") long first, @Bind("last") long last, @Bind("total") long total, @Bind("sessions") int sessions) {
        throw new UnsupportedDatabaseOperationException();
    }

}