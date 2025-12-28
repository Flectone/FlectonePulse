package net.flectone.pulse.data.database.sql;

import org.jdbi.v3.sqlobject.config.KeyColumn;
import org.jdbi.v3.sqlobject.config.ValueColumn;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Map;

/**
 * SQL interface for player setting data operations in FlectonePulse.
 * Defines database queries for managing player settings and preferences.
 *
 * @author TheFaser
 * @since 1.6.0
 */
public interface SettingSQL extends SQL {

    /**
     * Finds all settings for a player.
     *
     * @param playerId the player ID
     * @return map of setting types to their values
     */
    @KeyColumn("type")
    @ValueColumn("value")
    @SqlQuery("SELECT `type`, `value` FROM `fp_setting` WHERE `player` = :player")
    Map<String, String> findByPlayer(@Bind("player") int playerId);

    /**
     * Inserts a new player setting.
     *
     * @param playerId the player ID
     * @param type the setting type
     * @param value the setting value
     */
    @SqlUpdate("INSERT INTO `fp_setting` (`player`, `type`, `value`) VALUES (:player, :type, :value)")
    void insert(@Bind("player") int playerId, @Bind("type") String type, @Bind("value") String value);

    /**
     * Updates an existing player setting.
     *
     * @param playerId the player ID
     * @param type the setting type
     * @param value the new setting value
     * @return the number of rows updated
     */
    @SqlUpdate("UPDATE `fp_setting` SET `value` = :value WHERE `player` = :player AND `type` = :type")
    int update(@Bind("player") int playerId, @Bind("type") String type, @Bind("value") String value);

}