package net.flectone.pulse.data.database.sql.fcolor;

import net.flectone.pulse.data.database.dao.FColorDao;
import net.flectone.pulse.data.database.sql.SQL;
import net.flectone.pulse.exception.UnsupportedDatabaseOperationException;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.statement.Query;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SQL interface for player color data operations in FlectonePulse.
 * Defines database queries for managing player color configurations.
 *
 * @author TheFaser
 * @since 0.9.0
 */
public interface FColorSQL extends SQL {

    @SqlQuery("SELECT `number`, `fp_fcolor`.`name`, `type` FROM `fp_player_fcolor` LEFT JOIN `fp_fcolor` ON `fp_player_fcolor`.`fcolor` = `fp_fcolor`.`id` WHERE `fp_player_fcolor`.`player` = :playerId")
    List<FColorDao.FColorInfo> findFColors(@Bind("playerId") int playerId);

    // idk why this doesn't work
    // @SqlQuery("SELECT `name`, `id` FROM `fp_fcolor` WHERE `name` IN (`<names>`)")
    // @KeyColumn("name")
    // @ValueColumn("id")
    // Map<String, Integer> findFColorIdsByNames(@BindList("names") List<String> names);
    default Map<String, Integer> findFColorIdsByNames(Handle handle, List<String> names) {
        if (names == null || names.isEmpty()) {
            return Map.of();
        }

        String placeholders = names.stream()
                .map(_ -> "?")
                .collect(Collectors.joining(", "));

        String sql = "SELECT `name`, `id` FROM `fp_fcolor` WHERE `name` IN (" + placeholders + ")";

        try (Query query = handle.createQuery(sql)) {
            for (int i = 0; i < names.size(); i++) {
                query.bind(i, names.get(i));
            }

            return query.reduceRows(new HashMap<>(), (map, rowView) -> {
                map.put(
                        rowView.getColumn("name", String.class),
                        rowView.getColumn("id", Integer.class)
                );

                return map;
            });
        }
    }

    @SqlUpdate("DELETE FROM `fp_player_fcolor` WHERE `player` = :playerId")
    void deleteFColors(@Bind("playerId") int playerId);

    @SqlUpdate("DELETE FROM `fp_player_fcolor` WHERE `player` = :playerId AND `type` = :type")
    void deleteFColors(@Bind("playerId") int playerId, @Bind("type") String type);

    @SqlUpdate("DELETE FROM `fp_player_fcolor` WHERE `player` = :playerId AND `type` = :type AND `number` IN (<numbers>)")
    void deleteFColors(@Bind("playerId") int playerId, @Bind("type") String type, @BindList("numbers") List<Integer> numbers);

    /**
     * Inserts color names into the database if they do not already exist.
     *
     * @param names list of color names to insert if absent
     * @throws UnsupportedDatabaseOperationException if not overridden
     */
    default void insertFColorsIfAbsent(@Bind("name") List<String> names) {
        throw new UnsupportedDatabaseOperationException();
    }

    /**
     * Performs batch upsert operations for player color configurations.
     * Inserts or updates multiple player color entries in a single operation,
     * associating color numbers with their corresponding color IDs and type.
     *
     * @param playerId the unique identifier of the player
     * @param numbers list of color slot numbers
     * @param fcolorIds list of color identifiers corresponding to each number
     * @param type the classification type of the colors
     * @throws UnsupportedDatabaseOperationException if not overridden
     */
    default void batchUpsertPlayerFColors(@Bind("playerId") int playerId, @Bind("number") List<Integer> numbers, @Bind("fcolorId") List<Integer> fcolorIds, @Bind("type") String type) {
        throw new UnsupportedDatabaseOperationException();
    }

}