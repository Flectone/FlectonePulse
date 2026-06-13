package net.flectone.pulse.data.database.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.data.database.Database;
import net.flectone.pulse.data.database.sql.fcolor.*;
import net.flectone.pulse.model.FColor;
import net.flectone.pulse.model.entity.FPlayer;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.mapper.Nested;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Data Access Object for color data operations in FlectonePulse.
 * Handles persistence and retrieval of player color preferences.
 *
 * @author TheFaser
 * @since 0.9.0
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FColorDao implements BaseDAO<FColorSQL> {

    private final Database database;

    @Override
    public Database database() {
        return database;
    }

    @Override
    public Class<? extends FColorSQL> sqlClass() {
        return switch (database.config().type()) {
            case H2 -> FColorH2.class;
            case MARIADB -> FColorMariaDB.class;
            case MYSQL -> FColorMySQL.class;
            case POSTGRESQL -> FColorPostgreSQL.class;
            case SQLITE -> FColorSQLite.class;
        };
    }

    public void save(@NonNull FPlayer fPlayer, @NonNull Map<FColor.Type, Set<FColor>> colors) {
        if (database.isClosed()) return;
        if (colors.isEmpty()) {
            delete(fPlayer);
            return;
        }

        useCustomTransaction(handle -> {
            FColorSQL sql = getSQL(handle);

            Map<FColor.Type, Set<FColor>> oldFColors = findFColors(sql, fPlayer);
            if (colors.equals(oldFColors)) return;

            if (colors.isEmpty()) {
                sql.deleteFColors(fPlayer.id());
                return;
            }

            Arrays.stream(FColor.Type.values()).forEach(type ->
                    saveType(handle, sql, fPlayer, type, colors.getOrDefault(type, Set.of()), oldFColors.getOrDefault(type, Set.of()))
            );
        });
    }

    /**
     * Deletes the player's color preferences from the database.
     *
     * @param fPlayer the player whose colors to delete
     */
    public void delete(@NonNull FPlayer fPlayer) {
        if (database.isClosed()) return;

        useHandle(sql -> sql.deleteFColors(fPlayer.id()));
    }

    /**
     * Loads the player's color preferences from the database.
     *
     * @param fPlayer the player whose colors to load
     * @return new FPlayer with colors
     */
    public Map<FColor.Type, Set<FColor>> load(@NonNull FPlayer fPlayer) {
        if (database.isClosed()) return Map.of();
        if (fPlayer.isUnknown()) return Map.of();

        return withHandle(sql -> findFColors(sql, fPlayer));
    }

    private Map<FColor.Type, Set<FColor>> findFColors(FColorSQL sql, FPlayer fPlayer) {
        return sql.findFColors(fPlayer.id()).stream()
                .collect(Collectors.groupingBy(
                        FColorInfo::type,
                        Collectors.mapping(
                                FColorInfo::fColor,
                                Collectors.toSet()
                        )
                ));
    }

    private void saveType(Handle handle, FColorSQL sql, FPlayer fPlayer, FColor.Type type, @NonNull Set<FColor> newFColors, @NonNull Set<FColor> oldFColors) {
        if (newFColors.equals(oldFColors)) return;
        if (newFColors.isEmpty()) {
            sql.deleteFColors(fPlayer.id(), type.name());
            return;
        }

        List<FColor> toUpsert = newFColors.stream()
                .filter(c -> !oldFColors.contains(c))
                .toList();

        Set<Integer> newNumbers = newFColors.stream()
                .map(FColor::number)
                .collect(Collectors.toSet());

        List<Integer> toDelete = oldFColors.stream()
                .map(FColor::number)
                .filter(n -> !newNumbers.contains(n))
                .toList();

        if (toUpsert.isEmpty() && toDelete.isEmpty()) return;

        if (!toUpsert.isEmpty()) {
            List<String> names = toUpsert.stream().map(FColor::name).distinct().toList();

            sql.insertFColorsIfAbsent(names);

            Map<String, Integer> nameToId = sql.findFColorIdsByNames(handle, names);

            sql.batchUpsertPlayerFColors(
                    fPlayer.id(),
                    toUpsert.stream().map(FColor::number).toList(),
                    toUpsert.stream().map(fColor -> nameToId.get(fColor.name())).toList(),
                    type.name()
            );
        }

        if (!toDelete.isEmpty()) {
            sql.deleteFColors(fPlayer.id(), type.name(), toDelete);
        }

    }

    /**
     * Represents colors information retrieved from the database.
     *
     * @param fColor the color instance containing color details
     * @param type the classification type of the color
     */
    public record FColorInfo(
            @NonNull
            @Nested
            FColor fColor,
            FColor.@NonNull Type type
    ) {
    }

}