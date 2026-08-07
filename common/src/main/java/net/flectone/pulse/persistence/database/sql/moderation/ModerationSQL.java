package net.flectone.pulse.persistence.database.sql.moderation;

import net.flectone.pulse.model.value.Moderation;
import net.flectone.pulse.persistence.database.sql.SQL;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;

public interface ModerationSQL extends SQL {

    @SqlQuery("SELECT * FROM `fp_moderation` WHERE `player` = :player AND `type` = :type AND `valid` = true AND (`time` = -1 OR `time` > :currentTime) AND (:server IS NULL OR `server` IS NULL OR `server` = :server) ORDER BY `id` DESC LIMIT :limit OFFSET :offset")
    List<Moderation> findValidByPlayerAndType(@Bind("player") int playerId, @Bind("type") String type, @Bind("currentTime") long currentTime, @Bind("server") String server, @Bind("limit") int limit, @Bind("offset") int offset);

    @SqlQuery("SELECT * FROM `fp_moderation` WHERE `type` = :type AND `valid` = true AND (`time` = -1 OR `time` > :currentTime) AND (:server IS NULL OR `server` IS NULL OR `server` = :server) AND NOT `player` = -1 ORDER BY `id` DESC LIMIT :limit OFFSET :offset")
    List<Moderation> findValidByType(@Bind("type") String type, @Bind("currentTime") long currentTime, @Bind("server") String server, @Bind("limit") int limit, @Bind("offset") int offset);

    @SqlQuery("SELECT * FROM `fp_moderation` WHERE `valid` = true AND (`time` = -1 OR `time` > :currentTime) AND (:server IS NULL OR `server` IS NULL OR `server` = :server) AND `id` = :id")
    Optional<Moderation> findValidById(@Bind("currentTime") long currentTime, @Bind("server") String server, @Bind("id") int id);

    @SqlQuery("SELECT `p`.`name` FROM `fp_moderation` `m` JOIN `fp_player` `p` ON `p`.`id` = `m`.`player` WHERE `m`.`type` = :type AND `m`.`valid` = true AND (`m`.`time` = -1 OR `m`.`time` > :currentTime) AND (:server IS NULL OR `m`.`server` IS NULL OR `m`.`server` = :server) AND NOT `player` = -1")
    List<String> findValidPlayerNamesByType(@Bind("type") String type, @Bind("currentTime") long currentTime, @Bind("server") String server);

    @SqlQuery("SELECT COUNT(id) FROM `fp_moderation` WHERE `player` = :player AND `type` = :type AND `valid` = true AND (`time` = -1 OR `time` > :currentTime) AND (:server IS NULL OR `server` IS NULL OR `server` = :server)")
    int getTotalValidCountByPlayerAndType(@Bind("player") int playerId, @Bind("type") String type, @Bind("currentTime") long currentTime, @Bind("server") String server);

    @SqlQuery("SELECT COUNT(id) FROM `fp_moderation` WHERE `type` = :type AND `valid` = true AND (`time` = -1 OR `time` > :currentTime) AND (:server IS NULL OR `server` IS NULL OR `server` = :server) AND NOT `player` = -1")
    int getTotalValidCountByType(@Bind("type") String type, @Bind("currentTime") long currentTime, @Bind("server") String server);

    @GetGeneratedKeys("id")
    @SqlUpdate("INSERT INTO `fp_moderation` (`player`, `date`, `time`, `reason`, `moderator`, `type`, `valid`, `server`) VALUES (:player, :date, :time, :reason, :moderator, :type, true, :server)")
    int insert(@Bind("player") int playerId, @Bind("date") long date, @Bind("time") long time, @Bind("reason") String reason, @Bind("moderator") int moderatorId, @Bind("type") String type, @Bind("server") String server);

    @SqlUpdate("UPDATE `fp_moderation` SET `valid` = false WHERE `id` = :id AND (:server IS NULL OR `server` IS NULL OR `server` = :server)")
    void invalidate(@Bind("id") int id, @Bind("server") String server);

    @SqlUpdate("UPDATE `fp_moderation` SET `valid` = false WHERE `player` = :player AND `type` = :type AND (:server IS NULL OR `server` IS NULL OR `server` = :server)")
    void invalidate(@Bind("player") int playerId, @Bind("type") String type, @Bind("server") String server);

}