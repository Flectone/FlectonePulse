package net.flectone.pulse.database.sql;

import net.flectone.pulse.model.Moderation;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

public interface ModerationSQL extends SQL {

    @SqlQuery("SELECT * FROM `moderation` WHERE `player` = :player AND `type` = :type")
    List<Moderation> findByPlayerAndType(@Bind("player") int playerId, @Bind("type") int type);

    @SqlQuery("SELECT * FROM `moderation` WHERE `player` = :player AND `type` = :type AND `valid` = true AND (`time` = -1 OR `time` > :currentTime)")
    List<Moderation> findValidByPlayerAndType(@Bind("player") int playerId, @Bind("type") int type, @Bind("currentTime") long currentTime);

    @SqlQuery("SELECT * FROM `moderation` WHERE `type` = :type AND `valid` = true AND (`time` = -1 OR `time` > :currentTime)")
    List<Moderation> findValidByType(@Bind("type") int type, @Bind("currentTime") long currentTime);

    @SqlQuery("SELECT `p`.`name` FROM `moderation` `m` JOIN `player` `p` ON `p`.`id` = `m`.`player` WHERE `m`.`type` = :type AND `m`.`valid` = true AND (`m`.`time` = -1 OR `m`.`time` > :currentTime)")
    List<String> findValidPlayerNamesByType(@Bind("type") int type, @Bind("currentTime") long currentTime);

    @SqlUpdate("INSERT INTO `moderation` (`player`, `date`, `time`, `reason`, `moderator`, `type`, `valid`) VALUES (:player, :date, :time, :reason, :moderator, :type, true)")
    @GetGeneratedKeys("id")
    int insert(@Bind("player") int playerId, @Bind("date") long date, @Bind("time") long time, @Bind("reason") String reason, @Bind("moderator") int moderatorId, @Bind("type") int type);

    @SqlUpdate("UPDATE `moderation` SET `valid` = false WHERE `id` = :id")
    void invalidate(@Bind("id") int id);

}
